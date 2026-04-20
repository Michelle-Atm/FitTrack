package com.example.fitrack.model

enum class HeureRepas(val valeur: String) {
    PETIT_DEJEUNER("petit-dejeuner"),
    DEJEUNER("dejeuner"),
    DINER("diner"),
    COLLATION("collation");

    companion object {
        fun fromValeur(v: String) = entries.firstOrNull { it.valeur == v } ?: COLLATION
    }
}

data class Repas(
    val id: String = "",
    val userId: String = "",
    val nom: String = "",
    val date: Long = 0L,
    val heure: String = "",      // HeureRepas.valeur : "petit-dejeuner" | "dejeuner" | "diner" | "collation"
    val calories: Double = 0.0,  // kcal pour quantiteG grammes
    val proteines: Double = 0.0, // g pour quantiteG grammes
    val glucides: Double = 0.0,  // g pour quantiteG grammes
    val lipides: Double = 0.0,   // g pour quantiteG grammes
    val fibres: Double = 0.0,    // g pour quantiteG grammes
    val photoUrl: String = "",
    val codeBarres: String = "",
    val quantiteG: Double = 100.0 // quantité consommée en grammes (les valeurs nutritionnelles sont scalées à cette quantité)
)
