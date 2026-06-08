package com.example.fitrack.repository

import com.example.fitrack.model.ProfilPublic
import kotlinx.coroutines.flow.Flow

interface SocialRepository {
    suspend fun lireClassement(): Result<List<ProfilPublic>>
    suspend fun mettreAJourProfil(profil: ProfilPublic): Result<Unit>
    fun observerClassement(): Flow<List<ProfilPublic>>
    suspend fun lireProfilPublic(userId: String): Result<ProfilPublic>
}
