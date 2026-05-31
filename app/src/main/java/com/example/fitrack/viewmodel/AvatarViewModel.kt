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

    private val _xpPourNiveau = MutableStateFlow(300)
    val xpPourNiveau: StateFlow<Int> = _xpPourNiveau.asStateFlow()

    private val _historiqueEvolution = MutableStateFlow<List<String>>(emptyList())
    val historiqueEvolution: StateFlow<List<String>> = _historiqueEvolution.asStateFlow()

    fun chargerAvatar(uid: String) {
        viewModelScope.launch {
            authRepository.recupererProfil(uid).onSuccess { user ->
                mettreAJourUi(user)
            }
        }
    }

    fun ajouterXp(uid: String, xpGagne: Int) {
        viewModelScope.launch {
            authRepository.recupererProfil(uid).onSuccess { user ->
                val nouvelleXp = user.xp + xpGagne
                val nouveauNiveau = (nouvelleXp / 300) + 1

                val userMisAJour = user.copy(
                    xp = nouvelleXp,
                    niveau = nouveauNiveau
                )

                authRepository.mettreAJourProfil(userMisAJour).onSuccess {
                    mettreAJourUi(userMisAJour)
                }
            }
        }
    }

    private fun mettreAJourUi(user: User) {
        val etat = determinerEtatActuel(user.xp)

        _avatar.value = Avatar(
            userId = user.uid,
            espece = "renard",
            niveau = user.niveau,
            etatActuel = etat,
            xpCumule = user.xp
        )

        _xpPourNiveau.value = user.niveau * 300
        _prochainNiveau.value = user.niveau + 1
        _xpProgression.value = (user.xp % 300).toFloat() / 300f

        _historiqueEvolution.value = listOf("Niveau ${user.niveau} atteint")
    }

    private fun determinerEtatActuel(xp: Int): String {
        return when {
            xp < 100 -> "triste"
            xp in 100..500 -> "neutre"
            xp in 501..1000 -> "heureux"
            else -> "champion"
        }
    }

    companion object {
        val Factory: androidx.lifecycle.ViewModelProvider.Factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                return AvatarViewModel(com.example.fitrack.repository.firestore.FirestoreAuthRepository()) as T
            }
        }
    }
}