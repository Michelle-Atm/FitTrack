package com.example.fitrack.repository.firestore

import com.example.fitrack.model.ProfilPublic
import com.example.fitrack.repository.SocialRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreSocialRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : SocialRepository {

    companion object {
        private const val COLLECTION = "profilsPublics"
    }

    override suspend fun lireClassement(): Result<List<ProfilPublic>> = try {
        val snapshot = db.collection(COLLECTION)
            .orderBy("scoreHebdo", Query.Direction.DESCENDING)
            .get()
            .await()
        Result.success(
            snapshot.documents.mapNotNull { doc ->
                doc.toObject(ProfilPublic::class.java)?.copy(userId = doc.id)
            }
        )
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun mettreAJourProfil(profil: ProfilPublic): Result<Unit> = try {
        db.collection(COLLECTION)
            .document(profil.userId)
            .set(profil)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
