package com.example.fitrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitrack.model.User
import com.example.fitrack.repository.AuthRepository
import com.example.fitrack.repository.firestore.FirestoreAuthRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class AuthViewModel(
    private val authRepository: AuthRepository = FirestoreAuthRepository()
) : ViewModel() {

    sealed class AuthUiState {
        object Initial : AuthUiState()
        object Chargement : AuthUiState()
        data class Succes(val utilisateur: User) : AuthUiState()
        data class Erreur(val message: String) : AuthUiState()
        object Deconnecte : AuthUiState()
    }

    sealed class AuthEvenement {
        object ProfilMisAJour : AuthEvenement()
        data class Erreur(val message: String) : AuthEvenement()
    }

    private val _uiState = MutableStateFlow<AuthUiState>(AuthUiState.Initial)
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    private val _evenements = MutableSharedFlow<AuthEvenement>()
    val evenements: SharedFlow<AuthEvenement> = _evenements.asSharedFlow()

    val utilisateurActuel: StateFlow<User?> = authRepository
        .observerUtilisateur()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun connexion(email: String, motDePasse: String) {
        if (email.isBlank() || motDePasse.isBlank()) {
            _uiState.value = AuthUiState.Erreur("Email et mot de passe requis")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Chargement
            authRepository.connexion(email.trim(), motDePasse)
                .onSuccess { _uiState.value = AuthUiState.Succes(it) }
                .onFailure { _uiState.value = AuthUiState.Erreur(it.message ?: "Erreur de connexion") }
        }
    }

    fun inscrire(email: String, motDePasse: String, user: User) {
        if (email.isBlank() || motDePasse.isBlank() || user.nom.isBlank()) {
            _uiState.value = AuthUiState.Erreur("Tous les champs obligatoires doivent être remplis")
            return
        }
        viewModelScope.launch {
            _uiState.value = AuthUiState.Chargement
            val imc = calculerIMC(user.poids, user.taille)
            val userComplet = user.copy(
                imc = imc,
                programmePersonnalise = genererProgramme(user.copy(imc = imc))
            )
            authRepository.inscription(email.trim(), motDePasse, userComplet)
                .onSuccess { _uiState.value = AuthUiState.Succes(it) }
                .onFailure { _uiState.value = AuthUiState.Erreur(it.message ?: "Erreur d'inscription") }
        }
    }

    fun mettreAJourProfil(user: User) {
        viewModelScope.launch {
            _uiState.value = AuthUiState.Chargement
            val imc = calculerIMC(user.poids, user.taille)
            val userMisAJour = user.copy(
                imc = imc,
                programmePersonnalise = genererProgramme(user.copy(imc = imc))
            )
            authRepository.mettreAJourProfil(userMisAJour)
                .onSuccess {
                    _uiState.value = AuthUiState.Initial
                    _evenements.emit(AuthEvenement.ProfilMisAJour)
                }
                .onFailure {
                    val msg = it.message ?: "Erreur de mise à jour"
                    _uiState.value = AuthUiState.Erreur(msg)
                    _evenements.emit(AuthEvenement.Erreur(msg))
                }
        }
    }

    fun deconnexion() {
        viewModelScope.launch {
            authRepository.deconnexion()
            _uiState.value = AuthUiState.Deconnecte
        }
    }

    fun reinitialiserEtat() {
        _uiState.value = AuthUiState.Initial
    }

    // --- Logique métier pure (testable sans Firebase) ---

    fun calculerIMC(poids: Double, taille: Int): Double {
        if (poids <= 0.0 || taille <= 0) return 0.0
        val tailleM = taille / 100.0
        return Math.round(poids / (tailleM * tailleM) * 10) / 10.0
    }

    fun categorieIMC(imc: Double): String = when {
        imc <= 0.0 -> "Non calculé"
        imc < 18.5 -> "Sous-poids"
        imc < 25.0 -> "Poids normal"
        imc < 30.0 -> "Surpoids"
        imc < 35.0 -> "Obésité modérée"
        else -> "Obésité sévère"
    }

    fun genererProgramme(user: User): String {
        val jours = user.disponibilites.size.coerceAtLeast(1)
        val allergiesNote = if (user.allergies.isNotEmpty())
            " (sans ${user.allergies.joinToString(", ")})" else ""

        return when {
            user.objectif == "perte_poids" && user.experience == "debutant" ->
                "Programme Brûle-graisse Débutant — $jours séance(s)/sem : " +
                "Cardio 30min (marche rapide/vélo) + renforcement corps entier faible charge$allergiesNote"

            user.objectif == "perte_poids" && user.experience == "intermediaire" ->
                "Programme Brûle-graisse Intermédiaire — $jours séance(s)/sem : " +
                "HIIT 20min + circuit training 3 séries$allergiesNote"

            user.objectif == "perte_poids" && user.experience == "avance" ->
                "Programme Brûle-graisse Avancé — $jours séance(s)/sem : " +
                "HIIT 30min + PPL allégé + déficit calorique 15%$allergiesNote"

            user.objectif == "prise_masse" && user.experience == "debutant" ->
                "Programme Prise de masse Débutant — $jours séance(s)/sem : " +
                "Full body 3x, squats/développé/tirage, progression linéaire$allergiesNote"

            user.objectif == "prise_masse" && user.experience == "intermediaire" ->
                "Programme Hypertrophie Intermédiaire — $jours séance(s)/sem : " +
                "PPL 2x, 4 séries × 8-12 reps, surplus calorique 10%$allergiesNote"

            user.objectif == "prise_masse" && user.experience == "avance" ->
                "Programme Hypertrophie Avancé — $jours séance(s)/sem : " +
                "PPL 6j, périodisation ondulante, surplus 5-8%$allergiesNote"

            user.objectif == "endurance" ->
                "Programme Endurance — $jours séance(s)/sem : " +
                "Course progressive (80% zone 2) + 1 séance seuil lactate$allergiesNote"

            else ->
                "Programme Équilibre — $jours séance(s)/sem : " +
                "Cardio 2x + renforcement musculaire 2x, alimentation équilibrée$allergiesNote"
        }
    }

    class Factory(private val repository: AuthRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            AuthViewModel(repository) as T
    }
}
