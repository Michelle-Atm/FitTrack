package com.example.fitrack.viewmodel

import androidx.lifecycle.ViewModel
import com.example.fitrack.model.Avatar
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Stub temporaire — à remplacer dès que P2 merge AvatarViewModel Sprint 2
class AvatarViewModel : ViewModel() {

    private val _avatar = MutableStateFlow<Avatar?>(
        Avatar(espece = "renard", niveau = 1, etatActuel = "neutre", xpCumule = 120)
    )
    val avatar: StateFlow<Avatar?> = _avatar.asStateFlow()

    private val _xpProgression = MutableStateFlow(0.4f)
    val xpProgression: StateFlow<Float> = _xpProgression.asStateFlow()

    private val _prochainNiveau = MutableStateFlow(2)
    val prochainNiveau: StateFlow<Int> = _prochainNiveau.asStateFlow()

    private val _xpPourNiveau = MutableStateFlow(300)
    val xpPourNiveau: StateFlow<Int> = _xpPourNiveau.asStateFlow()

    private val _historiqueEvolution = MutableStateFlow<List<String>>(
        listOf("Niveau 1 atteint · J1")
    )
    val historiqueEvolution: StateFlow<List<String>> = _historiqueEvolution.asStateFlow()
}
