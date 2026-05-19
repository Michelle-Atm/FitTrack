package com.example.fitrack.viewmodel

import androidx.lifecycle.ViewModel
import com.example.fitrack.model.ProfilPublic
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Stub temporaire — à remplacer dès que P2 merge SocialViewModel Sprint 2
class SocialViewModel : ViewModel() {

    private val _maCategorie = MutableStateFlow("Débutant")
    val maCategorie: StateFlow<String> = _maCategorie.asStateFlow()

    private val _monRang = MutableStateFlow(7)
    val monRang: StateFlow<Int> = _monRang.asStateFlow()

    private val _monScoreHebdo = MutableStateFlow(1240.0)
    val monScoreHebdo: StateFlow<Double> = _monScoreHebdo.asStateFlow()

    private val _classementGlobal = MutableStateFlow<List<ProfilPublic>>(
        listOf(
            ProfilPublic("1", "Alice D.", "renard", "champion", 4200.0, 1),
            ProfilPublic("2", "Marc T.", "panda", "heureux", 3950.0, 2),
            ProfilPublic("3", "Sara K.", "pingouin", "champion", 3700.0, 3),
            ProfilPublic("4", "Leo B.", "axolotl", "neutre", 2800.0, 4),
            ProfilPublic("5", "Nina P.", "renard", "heureux", 2600.0, 5),
            ProfilPublic("6", "Tom G.", "panda", "triste", 2100.0, 6),
        )
    )
    val classementGlobal: StateFlow<List<ProfilPublic>> = _classementGlobal.asStateFlow()

    private val _classementCategorie = MutableStateFlow<List<ProfilPublic>>(emptyList())
    val classementCategorie: StateFlow<List<ProfilPublic>> = _classementCategorie.asStateFlow()

    val monUserId = "current_user"
}
