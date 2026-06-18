package com.example.fitrack.model

data class Invitation(
    val id: String = "",
    val envoyeurId: String = "",
    val envoyeurNom: String = "",
    val destinataireId: String = "",
    val statut: String = StatutInvitation.EN_ATTENTE,
    val timestamp: Long = 0L
) {
    object StatutInvitation {
        const val EN_ATTENTE = "en_attente"
        const val ACCEPTEE   = "acceptee"
        const val REFUSEE    = "refusee"
    }
}
