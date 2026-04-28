package com.example.fitrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitrack.model.Objectif
import com.example.fitrack.model.Seance
import com.example.fitrack.model.SideQuest
import com.example.fitrack.model.SideQuestUtilisateur
import com.example.fitrack.model.User
import com.example.fitrack.repository.ObjectifRepository
import com.example.fitrack.repository.firestore.FirestoreObjectifRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class ObjectifViewModel(
    private val objectifRepository: ObjectifRepository = FirestoreObjectifRepository()
) : ViewModel() {

    data class ProgressionJournaliere(
        val objectif: Objectif = Objectif(),
        val progressionCalories: Float = 0f,
        val progressionProteines: Float = 0f,
        val progressionGlucides: Float = 0f,
        val progressionLipides: Float = 0f,
        val progressionPas: Float = 0f,
        val progressionSeances: Float = 0f,
        val objectifsDepasseCalories: Boolean = false
    )

    sealed class ObjectifUiState {
        object Initial : ObjectifUiState()
        object Chargement : ObjectifUiState()
        data class Succes(val progression: ProgressionJournaliere) : ObjectifUiState()
        data class Erreur(val message: String) : ObjectifUiState()
    }

    sealed class SideQuestUiState {
        object Initial : SideQuestUiState()
        object Chargement : SideQuestUiState()
        data class Succes(
            val disponibles: List<SideQuest>,
            val utilisateur: List<SideQuestUtilisateur>
        ) : SideQuestUiState()
        data class Erreur(val message: String) : SideQuestUiState()
    }

    private val _objectifUiState = MutableStateFlow<ObjectifUiState>(ObjectifUiState.Initial)
    val objectifUiState: StateFlow<ObjectifUiState> = _objectifUiState.asStateFlow()

    private val _sideQuestUiState = MutableStateFlow<SideQuestUiState>(SideQuestUiState.Initial)
    val sideQuestUiState: StateFlow<SideQuestUiState> = _sideQuestUiState.asStateFlow()

    fun chargerObjectifJournalier(userId: String, date: Long = debutJournee()) {
        viewModelScope.launch {
            _objectifUiState.value = ObjectifUiState.Chargement
            objectifRepository.objectifJournalier(userId, date)
                .onSuccess { objectif ->
                    _objectifUiState.value = ObjectifUiState.Succes(calculerProgression(objectif))
                }
                .onFailure { e ->
                    val msg = e.message ?: ""
                    if (msg.contains("offline", ignoreCase = true) ||
                        msg.contains("UNAVAILABLE", ignoreCase = true)) {
                        _objectifUiState.value = ObjectifUiState.Succes(calculerProgression(Objectif()))
                    } else {
                        _objectifUiState.value = ObjectifUiState.Erreur(msg.ifBlank { "Erreur de chargement" })
                    }
                }
        }
    }

    fun loggerSeance(seance: Seance, userId: String) {
        viewModelScope.launch {
            objectifRepository.ajouterSeance(seance.copy(userId = userId))
                .onSuccess {
                    incrementerSeancesObjectif(userId)
                    verifierDeblocageSideQuests(userId)
                }
                .onFailure {
                    _objectifUiState.value = ObjectifUiState.Erreur(it.message ?: "Impossible de logger la séance")
                }
        }
    }

    fun chargerSideQuests(userId: String) {
        viewModelScope.launch {
            _sideQuestUiState.value = SideQuestUiState.Chargement
            val disponiblesResult = objectifRepository.sideQuestsDisponibles()
            val utilisateurResult = objectifRepository.sideQuestsUtilisateur(userId)

            if (disponiblesResult.isSuccess && utilisateurResult.isSuccess) {
                _sideQuestUiState.value = SideQuestUiState.Succes(
                    disponibles = disponiblesResult.getOrThrow(),
                    utilisateur = utilisateurResult.getOrThrow()
                )
            } else {
                val erreur = disponiblesResult.exceptionOrNull() ?: utilisateurResult.exceptionOrNull()
                _sideQuestUiState.value = SideQuestUiState.Erreur(erreur?.message ?: "Erreur de chargement")
            }
        }
    }

    fun debloquerSideQuest(userId: String, questId: String) {
        viewModelScope.launch {
            objectifRepository.debloquerSideQuest(userId, questId)
                .onSuccess { chargerSideQuests(userId) }
                .onFailure {
                    _sideQuestUiState.value = SideQuestUiState.Erreur(it.message ?: "Impossible de débloquer")
                }
        }
    }

    fun completerSideQuest(userId: String, questId: String) {
        viewModelScope.launch {
            objectifRepository.completerSideQuest(userId, questId)
                .onSuccess { chargerSideQuests(userId) }
                .onFailure {
                    _sideQuestUiState.value = SideQuestUiState.Erreur(it.message ?: "Impossible de compléter")
                }
        }
    }

    // --- Logique métier pure ---

    fun calculerProgression(objectif: Objectif): ProgressionJournaliere {
        fun ratio(actuel: Double, cible: Double): Float =
            if (cible > 0) (actuel / cible).toFloat().coerceIn(0f, 1f) else 0f

        return ProgressionJournaliere(
            objectif = objectif,
            progressionCalories = ratio(objectif.caloriesActuelles, objectif.caloriesObjectif),
            progressionProteines = ratio(objectif.proteinesActuelles, objectif.proteinesObjectif),
            progressionGlucides = ratio(objectif.glucidesActuelles, objectif.glucidesObjectif),
            progressionLipides = ratio(objectif.lipidesActuelles, objectif.lipidesObjectif),
            progressionPas = ratio(objectif.pasActuels.toDouble(), objectif.pasObjectif.toDouble()),
            progressionSeances = ratio(objectif.seancesEffectuees.toDouble(), objectif.seancesObjectif.toDouble()),
            objectifsDepasseCalories = objectif.caloriesActuelles > objectif.caloriesObjectif * 1.1
        )
    }

    fun objectifAtteint(objectif: Objectif): Boolean =
        objectif.caloriesActuelles >= objectif.caloriesObjectif * 0.9 &&
        objectif.proteinesActuelles >= objectif.proteinesObjectif * 0.9 &&
        objectif.seancesEffectuees >= objectif.seancesObjectif

    fun conditionDeblocageRemplie(user: User, condition: String): Boolean = when {
        condition.startsWith("niveau_") ->
            user.niveau >= condition.removePrefix("niveau_").toIntOrNull() ?: Int.MAX_VALUE
        condition.startsWith("xp_") ->
            user.xp >= condition.removePrefix("xp_").toIntOrNull() ?: Int.MAX_VALUE
        else -> false
    }

    private fun debutJournee(): Long = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private fun incrementerSeancesObjectif(userId: String) {
        viewModelScope.launch {
            val date = debutJournee()
            objectifRepository.objectifJournalier(userId, date)
                .onSuccess { objectif ->
                    val objectifMaj = objectif.copy(
                        seancesEffectuees = objectif.seancesEffectuees + 1,
                        dateMAJ = System.currentTimeMillis()
                    )
                    objectifRepository.mettreAJourObjectif(objectifMaj)
                        .onSuccess {
                            _objectifUiState.value = ObjectifUiState.Succes(calculerProgression(objectifMaj))
                        }
                        .onFailure {
                            _objectifUiState.value = ObjectifUiState.Erreur(it.message ?: "Erreur de mise à jour")
                        }
                }
                .onFailure {
                    _objectifUiState.value = ObjectifUiState.Erreur(it.message ?: "Objectif journalier introuvable")
                }
        }
    }

    private fun verifierDeblocageSideQuests(userId: String) {
        chargerSideQuests(userId)
    }

    class Factory(private val repository: ObjectifRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            ObjectifViewModel(repository) as T
    }
}
