package com.example.fitrack.model

data class User(
    val uid: String = "",
    val name: String = "",
    val objectif: String = "",
    val imc: Double = 0.0,
    val experience: Int = 0,
    val niveau: Int = 1,
    val allergies: List<String> = emptyList(),
    val blessures: List<String> = emptyList() // Doit être emptyList()
)