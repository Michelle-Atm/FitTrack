package com.example.fitrack.model

// Stub temporaire — à remplacer dès que P1 merge son modèle Sprint 2
data class Avatar(
    val userId: String = "",
    val espece: String = "renard",      // "renard"|"pingouin"|"panda"|"axolotl"
    val niveau: Int = 1,                // 1 à 4
    val etatActuel: String = "neutre",  // "champion"|"heureux"|"neutre"|"triste"
    val xpCumule: Int = 0
)
