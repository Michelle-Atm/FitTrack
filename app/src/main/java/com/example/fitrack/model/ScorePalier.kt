package com.example.fitrack.model

data class ScorePalier(
    val id: String = "",
    val niveau: Int = 0,
    val label: String = "",
    val seuilMin: Int = 0,
    val seuilMax: Int = Int.MAX_VALUE
)
