package com.example.fitrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitrack.model.Objectif
import com.example.fitrack.model.Seance
import com.example.fitrack.model.SideQuest
import com.example.fitrack.model.SideQuestUtilisateur
import com.example.fitrack.model.User
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
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

    private val _celebrationEvent = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val celebrationEvent: SharedFlow<String> = _celebrationEvent.asSharedFlow()

    private val _seancesRecentes = MutableStateFlow<List<Seance>>(emptyList())
    val seancesRecentes: StateFlow<List<Seance>> = _seancesRecentes.asStateFlow()

    fun chargerSeancesRecentes(userId: String) {
        viewModelScope.launch {
            objectifRepository.lireSeancesRecentes(userId)
                .onSuccess { _seancesRecentes.value = it }
        }
    }

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
        val seanceAvecUser = seance.copy(userId = userId)
        viewModelScope.launch {
            objectifRepository.ajouterSeance(seanceAvecUser)
                .onSuccess {
                    // Optimistic: show the session immediately in the list
                    _seancesRecentes.value = (listOf(seanceAvecUser) + _seancesRecentes.value).take(10)
                    incrementerSeancesObjectif(userId)
                    verifierDeblocageSideQuests(userId)
                    // Background refresh to sync with Firestore
                    chargerSeancesRecentes(userId)
                }
                .onFailure {
                    android.util.Log.e("ObjectifViewModel", "Erreur lors du logging de séance: ", it)
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

    fun debloquerSideQuestsEligibles(userId: String, user: User) {
        viewModelScope.launch {
            val disponiblesResult = objectifRepository.sideQuestsDisponibles()
            val utilisateurResult = objectifRepository.sideQuestsUtilisateur(userId)
            if (!disponiblesResult.isSuccess || !utilisateurResult.isSuccess) return@launch

            val disponibles = disponiblesResult.getOrThrow()
            val utilisateur = utilisateurResult.getOrThrow()
            val debloqueesIds = utilisateur.filter { it.debloquee }.map { it.questId }.toSet()

            val aDebloquer = disponibles.filter { quest ->
                quest.id !in debloqueesIds && conditionDeblocageRemplie(user, quest.conditionDeblocage)
            }
            aDebloquer.forEach { quest ->
                objectifRepository.debloquerSideQuest(userId, quest.id)
            }
            if (aDebloquer.isNotEmpty()) chargerSideQuests(userId)
        }
    }

    // --- Logique métier pure ---

    // Score journalier = moyenne pondérée (calories 40% + pas 30% + séances 30%) × 1000
    // Score hebdo = Σ 7 jours, max théorique = 7 000
    fun calculerScoreHebdo(progressions: List<ProgressionJournaliere>): Int {
        if (progressions.isEmpty()) return 0
        return progressions.sumOf { p ->
            val scoreJour = p.progressionCalories * 0.4 +
                            p.progressionPas * 0.3 +
                            p.progressionSeances * 0.3
            (scoreJour * 1000).toInt()
        }
    }

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
            user.niveau >= (condition.removePrefix("niveau_").toIntOrNull() ?: Int.MAX_VALUE)
        condition.startsWith("xp_") ->
            user.xp >= (condition.removePrefix("xp_").toIntOrNull() ?: Int.MAX_VALUE)
        condition.startsWith("streak_") ->
            user.streakJours >= (condition.removePrefix("streak_").toIntOrNull() ?: Int.MAX_VALUE)
        condition == "premiere_seance" -> user.xp > 0
        condition.isBlank() -> false
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
            val courant = (_objectifUiState.value as? ObjectifUiState.Succes)?.progression?.objectif
            if (courant != null) {
                val maj = courant.copy(
                    seancesEffectuees = courant.seancesEffectuees + 1,
                    dateMAJ = System.currentTimeMillis()
                )
                // Optimistic update
                _objectifUiState.value = ObjectifUiState.Succes(calculerProgression(maj))
                if (objectifAtteint(maj)) _celebrationEvent.tryEmit("Objectif du jour atteint ! 🎉")
                objectifRepository.mettreAJourObjectif(maj)
                    .onFailure {
                        android.util.Log.e("ObjectifViewModel", "Erreur lors de la mise à jour de l'objectif: ", it)
                        // Rollback to previous state if Firestore write fails
                        _objectifUiState.value = ObjectifUiState.Succes(calculerProgression(courant))
                    }
            } else {
                // Fallback: read from Firestore if state not yet loaded
                objectifRepository.objectifJournalier(userId, debutJournee())
                    .onSuccess { objectif ->
                        val maj = objectif.copy(
                            seancesEffectuees = objectif.seancesEffectuees + 1,
                            dateMAJ = System.currentTimeMillis()
                        )
                        objectifRepository.mettreAJourObjectif(maj)
                            .onSuccess {
                                _objectifUiState.value = ObjectifUiState.Succes(calculerProgression(maj))
                                if (objectifAtteint(maj)) _celebrationEvent.tryEmit("Objectif du jour atteint ! 🎉")
                            }
                            .onFailure {
                                android.util.Log.e("ObjectifViewModel", "Erreur lors du fallback de mise à jour de l'objectif: ", it)
                            }
                    }
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
