package com.example.fitrack.model

data class SideQuest(
    val id: String = "",
    val titre: String = "",
    val description: String = "",
    val type: String = "",               // "nutrition" | "sport" | "hydratation" | "sommeil"
    val conditionDeblocage: String = "", // "niveau_5" | "seances_10" | etc.
    val xpRecompense: Int = 0,
    val icone: String = ""
)

data class SideQuestUtilisateur(
    val userId: String = "",
    val questId: String = "",
    val debloquee: Boolean = false,
    val completee: Boolean = false,
    val dateDeblocage: Long = 0L,
    val dateCompletion: Long = 0L
)
