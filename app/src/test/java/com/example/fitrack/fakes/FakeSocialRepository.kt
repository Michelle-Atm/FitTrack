package com.example.fitrack.fakes

import com.example.fitrack.model.Invitation
import com.example.fitrack.model.ProfilPublic
import com.example.fitrack.repository.SocialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeSocialRepository : SocialRepository {
    var lireClassementResult: Result<List<ProfilPublic>> = Result.success(emptyList())
    var mettreAJourResult: Result<Unit> = Result.success(Unit)
    val profilsMisAJour = mutableListOf<ProfilPublic>()
    val invitationsEnvoyees = mutableListOf<Invitation>()
    var invitationsRecuesResult: Result<List<Invitation>> = Result.success(emptyList())

    override suspend fun lireClassement() = lireClassementResult

    override fun observerClassement(): Flow<List<ProfilPublic>> =
        flowOf(lireClassementResult.getOrDefault(emptyList()))

    override suspend fun lireProfilPublic(userId: String): Result<ProfilPublic> {
        val profil = lireClassementResult.getOrDefault(emptyList())
            .firstOrNull { it.userId == userId }
        return if (profil != null) Result.success(profil)
        else Result.failure(Exception("Profil introuvable"))
    }

    override suspend fun mettreAJourProfil(profil: ProfilPublic): Result<Unit> {
        profilsMisAJour.add(profil)
        return mettreAJourResult
    }

    override suspend fun envoyerInvitation(invitation: Invitation): Result<Unit> {
        invitationsEnvoyees.add(invitation)
        return Result.success(Unit)
    }

    override suspend fun mesInvitationsRecues(userId: String) = invitationsRecuesResult

    override suspend fun mesInvitationsEnvoyees(userId: String): Result<List<Invitation>> =
        Result.success(invitationsEnvoyees.filter { it.envoyeurId == userId })

    override suspend fun repondreInvitation(invitationId: String, accepte: Boolean): Result<Unit> =
        Result.success(Unit)
}
