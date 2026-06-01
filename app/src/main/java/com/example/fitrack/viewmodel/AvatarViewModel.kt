package com.example.fitrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fitrack.model.Avatar
import com.example.fitrack.model.User
import com.example.fitrack.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AvatarViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _avatar = MutableStateFlow<Avatar?>(null)
    val avatar: StateFlow<Avatar?> = _avatar.asStateFlow()

    private val _xpProgression = MutableStateFlow(0f)
    val xpProgression: StateFlow<Float> = _xpProgression.asStateFlow()

    private val _prochainNiveau = MutableStateFlow(2)
    val prochainNiveau: StateFlow<Int> = _prochainNiveau.asStateFlow()

    // XP nécessaire pour compléter le niveau courant — toujours 300 (fixe par niveau)
    private val _xpPourNiveau = MutableStateFlow(300)
    val xpPourNiveau: StateFlow<Int> = _xpPourNiveau.asStateFlow()

    private val _historiqueEvolution = MutableStateFlow<List<String>>(emptyList())
    val historiqueEvolution: StateFlow<List<String>> = _historiqueEvolution.asStateFlow()

    private val _isChargement = MutableStateFlow(false)
    val isChargement: StateFlow<Boolean> = _isChargement.asStateFlow()

    fun chargerAvatar(uid: String) {
        viewModelScope.launch {
            _isChargement.value = true
            authRepository.recupererProfil(uid).onSuccess { user ->
                mettreAJourUi(user)
            }
            _isChargement.value = false
        }
    }

    fun ajouterXp(uid: String, xpGagne: Int) {
        viewModelScope.launch {
            authRepository.recupererProfil(uid).onSuccess { user ->
                val nouvelleXp = user.xp + xpGagne
                val nouveauNiveau = calculerNiveau(nouvelleXp)
                val monteeDeNiveau = nouveauNiveau > user.niveau

                val userMisAJour = user.copy(xp = nouvelleXp, niveau = nouveauNiveau)

                authRepository.mettreAJourProfil(userMisAJour).onSuccess {
                    mettreAJourUi(userMisAJour)
                    if (monteeDeNiveau) {
                        _historiqueEvolution.value =
                            listOf("Niveau $nouveauNiveau atteint (+$xpGagne XP)") +
                            _historiqueEvolution.value
                    }
                }
            }
        }
    }

    private fun mettreAJourUi(user: User) {
        val xpDansNiveau = user.xp % XP_PAR_NIVEAU

        _avatar.value = Avatar(
            userId = user.uid,
            espece = "renard",
            niveau = user.niveau,
            etatActuel = determinerEtatActuel(xpDansNiveau),
            xpCumule = user.xp
        )

        _xpPourNiveau.value = XP_PAR_NIVEAU
        _prochainNiveau.value = user.niveau + 1
        _xpProgression.value = xpDansNiveau.toFloat() / XP_PAR_NIVEAU.toFloat()
    }

    // Visible pour les tests
    internal fun determinerEtatActuel(xpDansNiveau: Int): String = when {
        xpDansNiveau < 75  -> "triste"
        xpDansNiveau < 150 -> "neutre"
        xpDansNiveau < 225 -> "heureux"
        else               -> "champion"
    }

    internal fun calculerNiveau(xpTotal: Int): Int = (xpTotal / XP_PAR_NIVEAU) + 1

    companion object {
        const val XP_PAR_NIVEAU = 300

        val Factory: androidx.lifecycle.ViewModelProvider.Factory =
            object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T =
                    AvatarViewModel(
                        com.example.fitrack.repository.firestore.FirestoreAuthRepository()
                    ) as T
            }
    }
}
