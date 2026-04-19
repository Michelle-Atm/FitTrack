package com.example.fitrack.model

data class Objectif(
    val id: String = "",
    val userId: String = "",
    val date: Long = 0L,
    val caloriesObjectif: Double = 2000.0,
    val caloriesActuelles: Double = 0.0,
    val proteinesObjectif: Double = 150.0,
    val proteinesActuelles: Double = 0.0,
    val glucidesObjectif: Double = 250.0,
    val glucidesActuelles: Double = 0.0,
    val lipidesObjectif: Double = 65.0,
    val lipidesActuelles: Double = 0.0,
    val pasObjectif: Int = 10000,
    val pasActuels: Int = 0,
    val seancesObjectif: Int = 1,
    val seancesEffectuees: Int = 0
)
