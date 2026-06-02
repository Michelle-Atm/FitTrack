package com.example.fitrack.fakes

import com.example.fitrack.model.ProfilPublic
import com.example.fitrack.repository.SocialRepository

class FakeSocialRepository : SocialRepository {
    var lireClassementResult: Result<List<ProfilPublic>> = Result.success(emptyList())
    var mettreAJourResult: Result<Unit> = Result.success(Unit)
    val profilsMisAJour = mutableListOf<ProfilPublic>()

    override suspend fun lireClassement() = lireClassementResult

    override suspend fun mettreAJourProfil(profil: ProfilPublic): Result<Unit> {
        profilsMisAJour.add(profil)
        return mettreAJourResult
    }
}
