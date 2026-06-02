package com.example.fitrack.repository

import com.example.fitrack.model.ProfilPublic

interface SocialRepository {
    suspend fun lireClassement(): Result<List<ProfilPublic>>
    suspend fun mettreAJourProfil(profil: ProfilPublic): Result<Unit>
}
