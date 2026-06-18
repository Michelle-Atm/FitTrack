package com.example.fitrack.fakes

import com.example.fitrack.model.ScorePalier
import com.example.fitrack.model.User
import com.example.fitrack.repository.AdminRepository
import com.example.fitrack.repository.StatsGlobales

class FakeAdminRepository : AdminRepository {

    var utilisateurs: MutableList<User> = mutableListOf()
    var paliers: MutableList<ScorePalier> = mutableListOf()
    var stats: StatsGlobales = StatsGlobales()

    var lireUtilisateursResult: Result<List<User>>? = null
    var supprimerResult: Result<Unit> = Result.success(Unit)
    var suspendreResult: Result<Unit> = Result.success(Unit)
    var lirePaliersResult: Result<List<ScorePalier>>? = null
    var mettreAJourPalierResult: Result<Unit> = Result.success(Unit)
    var lireStatsResult: Result<StatsGlobales>? = null

    val supprimerAppels = mutableListOf<String>()
    val suspendreAppels = mutableListOf<Pair<String, Boolean>>()
    val mettreAJourPalierAppels = mutableListOf<ScorePalier>()

    override suspend fun lireTousLesUtilisateurs(): Result<List<User>> =
        lireUtilisateursResult ?: Result.success(utilisateurs.toList())

    override suspend fun supprimerUtilisateur(uid: String): Result<Unit> {
        supprimerAppels += uid
        return supprimerResult
    }

    override suspend fun suspendreUtilisateur(uid: String, suspendu: Boolean): Result<Unit> {
        suspendreAppels += uid to suspendu
        return suspendreResult
    }

    override suspend fun lirePaliers(): Result<List<ScorePalier>> =
        lirePaliersResult ?: Result.success(paliers.toList())

    override suspend fun mettreAJourPalier(palier: ScorePalier): Result<Unit> {
        mettreAJourPalierAppels += palier
        return mettreAJourPalierResult
    }

    override suspend fun lireStatsGlobales(): Result<StatsGlobales> =
        lireStatsResult ?: Result.success(stats)
}
