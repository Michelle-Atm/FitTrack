package com.example.fitrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitrack.model.AlimentOFF
import com.example.fitrack.model.Objectif
import com.example.fitrack.model.Repas
import com.example.fitrack.repository.NutritionRepository
import com.example.fitrack.repository.ObjectifRepository
import com.example.fitrack.repository.firestore.FirestoreNutritionRepository
import com.example.fitrack.repository.firestore.FirestoreObjectifRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class NutritionViewModel(
    private val nutritionRepository: NutritionRepository = FirestoreNutritionRepository(),
    private val objectifRepository: ObjectifRepository = FirestoreObjectifRepository()
) : ViewModel() {

    data class TotauxJournaliers(
        val calories: Double = 0.0,
        val proteines: Double = 0.0,
        val glucides: Double = 0.0,
        val lipides: Double = 0.0,
        val fibres: Double = 0.0
    )

    data class ComparaisonObjectifs(
        val pourcentageCalories: Float = 0f,
        val pourcentageProteines: Float = 0f,
        val pourcentageGlucides: Float = 0f,
        val pourcentageLipides: Float = 0f,
        val caloriesRestantes: Double = 0.0,
        val proteinesRestantes: Double = 0.0,
        val glucidesRestantes: Double = 0.0,
        val lipidesRestantes: Double = 0.0
    )

    sealed class NutritionUiState {
        object Initial : NutritionUiState()
        object Chargement : NutritionUiState()
        data class Succes(
            val repas: List<Repas>,
            val totaux: TotauxJournaliers,
            val comparaison: ComparaisonObjectifs = ComparaisonObjectifs()
        ) : NutritionUiState()
        data class Erreur(val message: String) : NutritionUiState()
    }

    sealed class RechercheState {
        object Idle : RechercheState()
        object Chargement : RechercheState()
        data class Resultats(val aliments: List<AlimentOFF>) : RechercheState()
        data class Vide(val message: String) : RechercheState()
        data class Erreur(val message: String) : RechercheState()
    }

    private val _uiState = MutableStateFlow<NutritionUiState>(NutritionUiState.Initial)
    val uiState: StateFlow<NutritionUiState> = _uiState.asStateFlow()

    private val _rechercheState = MutableStateFlow<RechercheState>(RechercheState.Idle)
    val rechercheState: StateFlow<RechercheState> = _rechercheState.asStateFlow()

    private var rechercheJob: Job? = null

    private val _historiqueRepas = MutableStateFlow<List<Repas>>(emptyList())
    val historiqueRepas: StateFlow<List<Repas>> = _historiqueRepas.asStateFlow()

    fun chargerRepasJournaliers(userId: String, date: Long = debutJournee()) {
        viewModelScope.launch {
            _uiState.value = NutritionUiState.Chargement
            nutritionRepository.repasJournalier(userId, date, date + 86_400_000L)
                .onSuccess { repas ->
                    val totaux = calculerTotaux(repas)
                    val comparaison = objectifRepository.objectifJournalier(userId, date)
                        .map { calculerComparaison(totaux, it) }
                        .getOrDefault(ComparaisonObjectifs())
                    _uiState.value = NutritionUiState.Succes(repas, totaux, comparaison)
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
                .onFailure { /* échec silencieux : l'historique est un affichage secondaire, pas bloquant */ }
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
        if (query.length < 2) {
            _rechercheState.value = RechercheState.Idle
            return
        }
        rechercheJob?.cancel()
        rechercheJob = viewModelScope.launch {
            delay(500L)
            _rechercheState.value = RechercheState.Chargement
            val termeLower = normaliser(query)
            nutritionRepository.rechercherAliment(query.trim())
                .onSuccess { aliments ->
                    val filtres = aliments.filter { aliment ->
                        aliment.nom.isNotBlank()
                            && (aliment.calories > 0 || aliment.proteines > 0 || aliment.glucides > 0)
                            && normaliser(aliment.nom).contains(termeLower)
                    }
                    if (filtres.isEmpty()) {
                        _rechercheState.value = RechercheState.Vide(
                            "Aucun aliment trouvé pour \"$query\""
                        )
                    } else {
                        _rechercheState.value = RechercheState.Resultats(filtres)
                    }
                }
                .onFailure { e ->
                    val msg = e.message ?: ""
                    _rechercheState.value = when {
                        msg.contains("503") ->
                            RechercheState.Erreur("Service indisponible, réessaie dans quelques secondes")
                        msg.contains("offline", ignoreCase = true) ||
                        msg.contains("Unable to resolve host", ignoreCase = true) ->
                            RechercheState.Erreur("Connexion impossible, vérifie ton réseau")
                        else -> RechercheState.Erreur(msg.ifBlank { "Erreur de recherche" })
                    }
                }
        }
    }

    private fun normaliser(s: String): String = s.lowercase()
        .replace("[àáâã]".toRegex(), "a")
        .replace("[éèêë]".toRegex(), "e")
        .replace("[îï]".toRegex(), "i")
        .replace("[ôõ]".toRegex(), "o")
        .replace("[ùûü]".toRegex(), "u")

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

    fun calculerComparaison(totaux: TotauxJournaliers, objectif: Objectif): ComparaisonObjectifs {
        fun pct(actuel: Double, cible: Double): Float =
            if (cible > 0) (actuel / cible).toFloat() else 0f

        return ComparaisonObjectifs(
            pourcentageCalories = pct(totaux.calories, objectif.caloriesObjectif),
            pourcentageProteines = pct(totaux.proteines, objectif.proteinesObjectif),
            pourcentageGlucides = pct(totaux.glucides, objectif.glucidesObjectif),
            pourcentageLipides = pct(totaux.lipides, objectif.lipidesObjectif),
            caloriesRestantes = objectif.caloriesObjectif - totaux.calories,
            proteinesRestantes = objectif.proteinesObjectif - totaux.proteines,
            glucidesRestantes = objectif.glucidesObjectif - totaux.glucides,
            lipidesRestantes = objectif.lipidesObjectif - totaux.lipides
        )
    }

    fun debutJournee(timestamp: Long = System.currentTimeMillis()): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    class Factory(
        private val nutritionRepository: NutritionRepository,
        private val objectifRepository: ObjectifRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            NutritionViewModel(nutritionRepository, objectifRepository) as T
    }
}
