package com.example.fitrack.model

data class AlimentOFF(
    val code: String = "",
    val nom: String = "",
    val calories: Double = 0.0,
    val proteines: Double = 0.0,
    val glucides: Double = 0.0,
    val lipides: Double = 0.0,
    val fibres: Double = 0.0,
    val imageUrl: String = "",
    val allergenes: List<String> = emptyList()
)
