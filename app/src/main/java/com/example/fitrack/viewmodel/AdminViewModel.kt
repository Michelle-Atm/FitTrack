package com.example.fitrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitrack.model.ScorePalier
import com.example.fitrack.model.User
import com.example.fitrack.repository.AdminRepository
import com.example.fitrack.repository.StatsGlobales
import com.example.fitrack.repository.firestore.FirestoreAdminRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AdminViewModel(
    private val adminRepository: AdminRepository = FirestoreAdminRepository()
) : ViewModel() {

    // ── États ─────────────────────────────────────────────────────────────────

    sealed class UtilisateursState {
        object Chargement : UtilisateursState()
        data class Succes(val utilisateurs: List<User>) : UtilisateursState()
        data class Erreur(val message: String) : UtilisateursState()
    }

    sealed class PaliersState {
        object Chargement : PaliersState()
        data class Succes(val paliers: List<ScorePalier>) : PaliersState()
        data class Erreur(val message: String) : PaliersState()
    }

    sealed class StatsState {
        object Chargement : StatsState()
        data class Succes(val stats: StatsGlobales) : StatsState()
        data class Erreur(val message: String) : StatsState()
    }

    private val _utilisateursState =
        MutableStateFlow<UtilisateursState>(UtilisateursState.Chargement)
    val utilisateursState: StateFlow<UtilisateursState> = _utilisateursState.asStateFlow()

    private val _paliersState =
        MutableStateFlow<PaliersState>(PaliersState.Chargement)
    val paliersState: StateFlow<PaliersState> = _paliersState.asStateFlow()

    private val _statsState =
        MutableStateFlow<StatsState>(StatsState.Chargement)
    val statsState: StateFlow<StatsState> = _statsState.asStateFlow()

    private val _actionEvenement = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val actionEvenement: SharedFlow<String> = _actionEvenement.asSharedFlow()

    // ── Utilisateurs ──────────────────────────────────────────────────────────

    fun chargerTousLesUtilisateurs() {
        viewModelScope.launch {
            _utilisateursState.value = UtilisateursState.Chargement
            adminRepository.lireTousLesUtilisateurs()
                .onSuccess { _utilisateursState.value = UtilisateursState.Succes(it) }
                .onFailure { _utilisateursState.value = UtilisateursState.Erreur(it.message ?: "Erreur de chargement") }
        }
    }

    fun supprimerUtilisateur(uid: String) {
        viewModelScope.launch {
            adminRepository.supprimerUtilisateur(uid)
                .onSuccess {
                    // Mise à jour optimiste : retire immédiatement de la liste locale
                    val courant = (_utilisateursState.value as? UtilisateursState.Succes)
                        ?.utilisateurs ?: return@onSuccess
                    _utilisateursState.value =
                        UtilisateursState.Succes(courant.filter { it.uid != uid })
                    _actionEvenement.emit("Utilisateur supprimé")
                }
                .onFailure { _actionEvenement.emit("Erreur : ${it.message}") }
        }
    }

    fun suspendreUtilisateur(uid: String, suspendu: Boolean) {
        viewModelScope.launch {
            adminRepository.suspendreUtilisateur(uid, suspendu)
                .onSuccess {
                    val courant = (_utilisateursState.value as? UtilisateursState.Succes)
                        ?.utilisateurs ?: return@onSuccess
                    _utilisateursState.value = UtilisateursState.Succes(
                        courant.map { if (it.uid == uid) it.copy(suspendu = suspendu) else it }
                    )
                    _actionEvenement.emit(if (suspendu) "Compte suspendu" else "Suspension levée")
                }
                .onFailure { _actionEvenement.emit("Erreur : ${it.message}") }
        }
    }

    // ── Paliers ───────────────────────────────────────────────────────────────

    val paliersDefaut = listOf(
        ScorePalier("palier_1", 1, "Débutant",      0,   299),
        ScorePalier("palier_2", 2, "Intermédiaire", 300, 599),
        ScorePalier("palier_3", 3, "Expert",         600, Int.MAX_VALUE)
    )

    fun chargerPaliers() {
        viewModelScope.launch {
            _paliersState.value = PaliersState.Chargement
            adminRepository.lirePaliers()
                .onSuccess { paliers ->
                    _paliersState.value = PaliersState.Succes(
                        paliers.ifEmpty { paliersDefaut }
                    )
                }
                .onFailure { _paliersState.value = PaliersState.Erreur(it.message ?: "Erreur de chargement") }
        }
    }

    fun mettreAJourPalier(palier: ScorePalier) {
        viewModelScope.launch {
            adminRepository.mettreAJourPalier(palier)
                .onSuccess {
                    val courant = (_paliersState.value as? PaliersState.Succes)
                        ?.paliers ?: return@onSuccess
                    _paliersState.value = PaliersState.Succes(
                        courant.map { if (it.id == palier.id) palier else it }
                    )
                    _actionEvenement.emit("Palier \"${palier.label}\" mis à jour")
                }
                .onFailure { _actionEvenement.emit("Erreur : ${it.message}") }
        }
    }

    // ── Statistiques ──────────────────────────────────────────────────────────

    fun chargerStats() {
        viewModelScope.launch {
            _statsState.value = StatsState.Chargement
            adminRepository.lireStatsGlobales()
                .onSuccess { _statsState.value = StatsState.Succes(it) }
                .onFailure { _statsState.value = StatsState.Erreur(it.message ?: "Erreur de chargement") }
        }
    }

    // ── Factory ───────────────────────────────────────────────────────────────

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T =
                AdminViewModel() as T
        }
    }
}
