package com.example.fitrack.model

data class ProfilPublic(
    val userId: String = "",
    val nom: String = "",
    val avatarEspece: String = "renard",
    val avatarEtat: String = "neutre",
    val scoreHebdo: Double = 0.0,
    val rang: Int = 0,
    val categorie: String = "debutant"   // mirrors User.experience
)
