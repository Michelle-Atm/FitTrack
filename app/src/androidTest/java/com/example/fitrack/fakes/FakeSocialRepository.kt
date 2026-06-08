package com.example.fitrack.fakes

import com.example.fitrack.model.ProfilPublic
import com.example.fitrack.repository.SocialRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeSocialRepository : SocialRepository {
    var lireClassementResult: Result<List<ProfilPublic>> = Result.success(emptyList())
    var mettreAJourResult: Result<Unit> = Result.success(Unit)
    val profilsMisAJour = mutableListOf<ProfilPublic>()

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
}
