package com.example.fitrack.repository.firestore

import com.example.fitrack.model.ScorePalier
import com.example.fitrack.model.User
import com.example.fitrack.repository.AdminRepository
import com.example.fitrack.repository.StatsGlobales
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class FirestoreAdminRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AdminRepository {

    companion object {
        private const val COLLECTION_USERS   = "users"
        private const val COLLECTION_SEANCES = "seances"
        private const val COLLECTION_PALIERS = "paliers"
    }

    override suspend fun lireTousLesUtilisateurs(): Result<List<User>> = try {
        val snap = db.collection(COLLECTION_USERS).get().await()
        Result.success(snap.toObjects(User::class.java))
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun supprimerUtilisateur(uid: String): Result<Unit> = try {
        db.collection(COLLECTION_USERS).document(uid).delete().await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun suspendreUtilisateur(uid: String, suspendu: Boolean): Result<Unit> = try {
        db.collection(COLLECTION_USERS).document(uid)
            .update("suspendu", suspendu).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun lirePaliers(): Result<List<ScorePalier>> = try {
        val snap = db.collection(COLLECTION_PALIERS).get().await()
        val paliers = snap.toObjects(ScorePalier::class.java).sortedBy { it.seuilMin }
        Result.success(paliers)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun mettreAJourPalier(palier: ScorePalier): Result<Unit> = try {
        db.collection(COLLECTION_PALIERS).document(palier.id).set(palier).await()
        Result.success(Unit)
    } catch (e: Exception) { Result.failure(e) }

    override suspend fun lireStatsGlobales(): Result<StatsGlobales> = try {
        val usersSnap   = db.collection(COLLECTION_USERS).get().await()
        val seancesSnap = db.collection(COLLECTION_SEANCES).get().await()
        val users       = usersSnap.toObjects(User::class.java)
        val scoreMoyen  = if (users.isNotEmpty()) users.sumOf { it.xp }.toDouble() / users.size else 0.0
        Result.success(
            StatsGlobales(
                totalUtilisateurs = users.size,
                totalSeances      = seancesSnap.size(),
                scoreMoyen        = scoreMoyen
            )
        )
    } catch (e: Exception) { Result.failure(e) }
}
