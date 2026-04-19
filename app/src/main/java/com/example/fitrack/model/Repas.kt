package com.example.fitrack.model

data class Repas(
    val id: String = "",
    val userId: String = "",
    val nom: String = "",
    val date: Long = 0L,
    val heure: String = "",      // "petit-dejeuner" | "dejeuner" | "diner" | "collation"
    val calories: Double = 0.0,
    val proteines: Double = 0.0, // g
    val glucides: Double = 0.0,  // g
    val lipides: Double = 0.0,   // g
    val fibres: Double = 0.0,    // g
    val photoUrl: String = "",
    val codeBarres: String = "",
    val quantiteG: Double = 100.0
)
