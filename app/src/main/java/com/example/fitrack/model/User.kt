package com.example.fitrack.model

data class User(
    val uid: String = "",
    val email: String = "",
    val nom: String = "",
    val objectif: String = "",           // "perte_poids" | "prise_masse" | "endurance" | "maintien"
    val poids: Double = 0.0,             // kg
    val taille: Int = 0,                 // cm
    val imc: Double = 0.0,
    val experience: String = "debutant", // niveau fitness : "debutant" | "intermediaire" | "avance" — détermine le programme
    val niveau: Int = 1,                 // niveau de progression dans le jeu (XP) — indépendant de l'expérience fitness
    val xp: Int = 0,                     // points d'expérience cumulés (avance niveau)
    val allergies: List<String> = emptyList(),
    val blessures: List<String> = emptyList(),
    val disponibilites: List<String> = emptyList(), // ["lundi", "mercredi", "vendredi"]
    val programmePersonnalise: String = "",
    val dateCreation: Long = 0L
)
