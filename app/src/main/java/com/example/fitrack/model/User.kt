package com.example.fitrack.model

data class User(
    val uid: String = "",
    val email: String = "",
    val nom: String = "",
    val objectif: String = "",           // "perte_poids" | "prise_masse" | "endurance" | "maintien"
    val poids: Double = 0.0,             // kg
    val taille: Int = 0,                 // cm
    val imc: Double = 0.0,
    val experience: String = "debutant", // "debutant" | "intermediaire" | "avance"
    val niveau: Int = 1,
    val xp: Int = 0,
    val allergies: List<String> = emptyList(),
    val blessures: List<String> = emptyList(),
    val disponibilites: List<String> = emptyList(), // ["lundi", "mercredi", "vendredi"]
    val programmePersonnalise: String = "",
    val dateCreation: Long = 0L
)
