package com.example.fitrack.repository

import com.example.fitrack.model.Invitation
import com.example.fitrack.model.ProfilPublic
import kotlinx.coroutines.flow.Flow

interface SocialRepository {
    suspend fun lireClassement(): Result<List<ProfilPublic>>
    suspend fun mettreAJourProfil(profil: ProfilPublic): Result<Unit>
    fun observerClassement(): Flow<List<ProfilPublic>>
    suspend fun lireProfilPublic(userId: String): Result<ProfilPublic>

    // Invitations
    suspend fun envoyerInvitation(invitation: Invitation): Result<Unit>
    suspend fun mesInvitationsRecues(userId: String): Result<List<Invitation>>
    suspend fun mesInvitationsEnvoyees(userId: String): Result<List<Invitation>>
    suspend fun repondreInvitation(invitationId: String, accepte: Boolean): Result<Unit>
}
