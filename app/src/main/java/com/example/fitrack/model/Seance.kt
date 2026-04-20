package com.example.fitrack.model

data class Seance(
    val id: String = "",
    val userId: String = "",
    val date: Long = 0L,
    val dureeMinutes: Int = 0,
    val type: String = "",          // "cardio" | "musculation" | "yoga" | "hiit" | "autre"
    val caloriesDepensees: Double = 0.0,
    val exercices: List<Exercice> = emptyList(),
    val notes: String = ""
)

data class Exercice(
    val nom: String = "",
    val series: Int = 0,
    val repetitions: Int = 0,
    val poidsKg: Double = 0.0,
    val dureeSecondes: Int = 0
)
