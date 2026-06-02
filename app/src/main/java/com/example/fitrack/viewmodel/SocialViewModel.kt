package com.example.fitrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitrack.model.ProfilPublic
import com.example.fitrack.repository.SocialRepository
import com.example.fitrack.repository.firestore.FirestoreSocialRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SocialViewModel(
    private val socialRepository: SocialRepository
) : ViewModel() {

    private val _maCategorie = MutableStateFlow("")
    val maCategorie: StateFlow<String> = _maCategorie.asStateFlow()

    private val _monRang = MutableStateFlow(0)
    val monRang: StateFlow<Int> = _monRang.asStateFlow()

    private val _monRangCategorie = MutableStateFlow(0)
    val monRangCategorie: StateFlow<Int> = _monRangCategorie.asStateFlow()

    private val _monScoreHebdo = MutableStateFlow(0.0)
    val monScoreHebdo: StateFlow<Double> = _monScoreHebdo.asStateFlow()

    private val _classementGlobal = MutableStateFlow<List<ProfilPublic>>(emptyList())
    val classementGlobal: StateFlow<List<ProfilPublic>> = _classementGlobal.asStateFlow()

    private val _classementCategorie = MutableStateFlow<List<ProfilPublic>>(emptyList())
    val classementCategorie: StateFlow<List<ProfilPublic>> = _classementCategorie.asStateFlow()

    private val _isChargement = MutableStateFlow(false)
    val isChargement: StateFlow<Boolean> = _isChargement.asStateFlow()

    var monUserId: String = ""
        private set

    fun chargerClassement(userId: String, categorie: String) {
        monUserId = userId
        _maCategorie.value = categorie
        viewModelScope.launch {
            _isChargement.value = true
            socialRepository.lireClassement().onSuccess { profils ->
                val global = profils
                    .sortedByDescending { it.scoreHebdo }
                    .mapIndexed { index, p -> p.copy(rang = index + 1) }
                _classementGlobal.value = global

                val monProfil = global.firstOrNull { it.userId == userId }
                _monRang.value = monProfil?.rang ?: 0
                _monScoreHebdo.value = monProfil?.scoreHebdo ?: 0.0

                val parCategorie = global
                    .filter { it.categorie == categorie }
                    .mapIndexed { index, p -> p.copy(rang = index + 1) }
                _classementCategorie.value = parCategorie
                _monRangCategorie.value = parCategorie.firstOrNull { it.userId == userId }?.rang ?: 0
            }
            _isChargement.value = false
        }
    }

    fun mettreAJourMonProfil(profil: ProfilPublic) {
        viewModelScope.launch {
            socialRepository.mettreAJourProfil(profil)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    SocialViewModel(FirestoreSocialRepository()) as T
            }
    }
}
