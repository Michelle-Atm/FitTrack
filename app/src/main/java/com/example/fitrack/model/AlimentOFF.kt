package com.example.fitrack.model

data class AlimentOFF(
    val code: String = "",
    val nom: String = "",
    val calories: Double = 0.0,      // kcal pour 100g
    val proteines: Double = 0.0,     // g pour 100g
    val glucides: Double = 0.0,      // g pour 100g
    val lipides: Double = 0.0,       // g pour 100g
    val fibres: Double = 0.0,        // g pour 100g
    val imageUrl: String = ""
)
