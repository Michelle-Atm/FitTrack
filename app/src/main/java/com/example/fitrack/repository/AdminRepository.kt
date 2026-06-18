package com.example.fitrack.repository

import com.example.fitrack.model.ScorePalier
import com.example.fitrack.model.User

data class StatsGlobales(
    val totalUtilisateurs: Int = 0,
    val totalSeances: Int = 0,
    val scoreMoyen: Double = 0.0
)

interface AdminRepository {
    suspend fun lireTousLesUtilisateurs(): Result<List<User>>
    suspend fun supprimerUtilisateur(uid: String): Result<Unit>
    suspend fun suspendreUtilisateur(uid: String, suspendu: Boolean): Result<Unit>
    suspend fun lirePaliers(): Result<List<ScorePalier>>
    suspend fun mettreAJourPalier(palier: ScorePalier): Result<Unit>
    suspend fun lireStatsGlobales(): Result<StatsGlobales>
}
