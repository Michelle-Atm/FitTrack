package com.example.fitrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitrack.model.AlimentOFF
import com.example.fitrack.model.Repas
import com.example.fitrack.repository.NutritionRepository
import com.example.fitrack.repository.firestore.FirestoreNutritionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class NutritionViewModel(
    private val nutritionRepository: NutritionRepository = FirestoreNutritionRepository()
) : ViewModel() {

    data class TotauxJournaliers(
        val calories: Double = 0.0,
        val proteines: Double = 0.0,
        val glucides: Double = 0.0,
        val lipides: Double = 0.0,
        val fibres: Double = 0.0
    )

    sealed class NutritionUiState {
        object Initial : NutritionUiState()
        object Chargement : NutritionUiState()
        data class Succes(val repas: List<Repas>, val totaux: TotauxJournaliers) : NutritionUiState()
        data class Erreur(val message: String) : NutritionUiState()
    }

    sealed class RechercheState {
        object Idle : RechercheState()
        object Chargement : RechercheState()
        data class Resultats(val aliments: List<AlimentOFF>) : RechercheState()
        data class Erreur(val message: String) : RechercheState()
    }

    private val _uiState = MutableStateFlow<NutritionUiState>(NutritionUiState.Initial)
    val uiState: StateFlow<NutritionUiState> = _uiState.asStateFlow()

    private val _rechercheState = MutableStateFlow<RechercheState>(RechercheState.Idle)
    val rechercheState: StateFlow<RechercheState> = _rechercheState.asStateFlow()

    private val _historiqueRepas = MutableStateFlow<List<Repas>>(emptyList())
    val historiqueRepas: StateFlow<List<Repas>> = _historiqueRepas.asStateFlow()

    fun chargerRepasJournaliers(userId: String, date: Long = debutJournee()) {
        viewModelScope.launch {
            _uiState.value = NutritionUiState.Chargement
            nutritionRepository.repasJournalier(userId, date, date + 86_400_000L)
                .onSuccess { repas ->
                    _uiState.value = NutritionUiState.Succes(repas, calculerTotaux(repas))
                }
                .onFailure {
                    _uiState.value = NutritionUiState.Erreur(it.message ?: "Erreur de chargement")
                }
        }
    }

    fun chargerHistorique(userId: String, jours: Int = 7) {
        viewModelScope.launch {
            nutritionRepository.historiqueRepas(userId, jours)
                .onSuccess { _historiqueRepas.value = it }
                .onFailure { /* historique silencieux */ }
        }
    }

    fun ajouterRepas(repas: Repas, userId: String) {
        viewModelScope.launch {
            nutritionRepository.ajouterRepas(repas.copy(userId = userId))
                .onSuccess { chargerRepasJournaliers(userId) }
                .onFailure {
                    _uiState.value = NutritionUiState.Erreur(it.message ?: "Impossible d'ajouter le repas")
                }
        }
    }

    fun supprimerRepas(repasId: String, userId: String) {
        viewModelScope.launch {
            nutritionRepository.supprimerRepas(repasId)
                .onSuccess { chargerRepasJournaliers(userId) }
                .onFailure {
                    _uiState.value = NutritionUiState.Erreur(it.message ?: "Impossible de supprimer le repas")
                }
        }
    }

    fun rechercherAliment(query: String) {
        if (query.isBlank()) {
            _rechercheState.value = RechercheState.Idle
            return
        }
        viewModelScope.launch {
            _rechercheState.value = RechercheState.Chargement
            nutritionRepository.rechercherAliment(query.trim())
                .onSuccess { _rechercheState.value = RechercheState.Resultats(it) }
                .onFailure { _rechercheState.value = RechercheState.Erreur(it.message ?: "Erreur de recherche") }
        }
    }

    fun rechercherParCodeBarres(code: String) {
        viewModelScope.launch {
            _rechercheState.value = RechercheState.Chargement
            nutritionRepository.rechercherParCodeBarres(code)
                .onSuccess { aliment ->
                    _rechercheState.value = if (aliment != null)
                        RechercheState.Resultats(listOf(aliment))
                    else
                        RechercheState.Erreur("Produit introuvable pour ce code-barres")
                }
                .onFailure { _rechercheState.value = RechercheState.Erreur(it.message ?: "Erreur de scan") }
        }
    }

    fun reinitialiserRecherche() {
        _rechercheState.value = RechercheState.Idle
    }

    // --- Logique métier pure ---

    fun calculerTotaux(repas: List<Repas>): TotauxJournaliers = TotauxJournaliers(
        calories = repas.sumOf { it.calories },
        proteines = repas.sumOf { it.proteines },
        glucides = repas.sumOf { it.glucides },
        lipides = repas.sumOf { it.lipides },
        fibres = repas.sumOf { it.fibres }
    )

    fun debutJournee(timestamp: Long = System.currentTimeMillis()): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    class Factory(private val repository: NutritionRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NutritionViewModel(repository) as T
    }
}
