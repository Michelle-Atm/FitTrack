package com.example.fitrack.repository.firestore

import com.example.fitrack.model.Objectif
import com.example.fitrack.model.Seance
import com.example.fitrack.model.SideQuest
import com.example.fitrack.model.SideQuestUtilisateur
import com.example.fitrack.repository.ObjectifRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await

class FirestoreObjectifRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ObjectifRepository {

    companion object {
        private const val COLLECTION_OBJECTIFS = "objectifs"
        private const val COLLECTION_SEANCES = "seances"
        private const val COLLECTION_SIDE_QUESTS = "sideQuests"
        private const val COLLECTION_SQ_UTILISATEURS = "sideQuestsUtilisateurs"
    }

    override suspend fun objectifJournalier(userId: String, date: Long): Result<Objectif> {
        val docId = "${userId}_$date"
        return try {
            val doc = db.collection(COLLECTION_OBJECTIFS).document(docId).get().await()
            val objectif = if (doc.exists()) {
                doc.toObject(Objectif::class.java) ?: Objectif(id = docId, userId = userId, date = date)
            } else {
                val nouveauObjectif = Objectif(id = docId, userId = userId, date = date)
                db.collection(COLLECTION_OBJECTIFS).document(docId).set(nouveauObjectif).await()
                nouveauObjectif
            }
            Result.success(objectif)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun mettreAJourObjectif(objectif: Objectif): Result<Unit> {
        return try {
            db.collection(COLLECTION_OBJECTIFS).document(objectif.id).set(objectif).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun ajouterSeance(seance: Seance): Result<Unit> {
        return try {
            val ref = db.collection(COLLECTION_SEANCES).document()
            db.collection(COLLECTION_SEANCES).document(ref.id).set(seance.copy(id = ref.id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun seancesUtilisateur(userId: String, dateDebut: Long, dateFin: Long): Result<List<Seance>> {
        return try {
            val snapshot = db.collection(COLLECTION_SEANCES)
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", dateDebut)
                .whereLessThan("date", dateFin)
                .orderBy("date", Query.Direction.DESCENDING)
                .get().await()
            Result.success(snapshot.toObjects(Seance::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sideQuestsDisponibles(): Result<List<SideQuest>> {
        return try {
            val snapshot = db.collection(COLLECTION_SIDE_QUESTS).get().await()
            Result.success(snapshot.toObjects(SideQuest::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun sideQuestsUtilisateur(userId: String): Result<List<SideQuestUtilisateur>> {
        return try {
            val snapshot = db.collection(COLLECTION_SQ_UTILISATEURS)
                .whereEqualTo("userId", userId)
                .get().await()
            Result.success(snapshot.toObjects(SideQuestUtilisateur::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun debloquerSideQuest(userId: String, questId: String): Result<Unit> {
        val docId = "${userId}_$questId"
        return try {
            val sqUtilisateur = SideQuestUtilisateur(
                userId = userId,
                questId = questId,
                debloquee = true,
                dateDeblocage = System.currentTimeMillis()
            )
            db.collection(COLLECTION_SQ_UTILISATEURS).document(docId).set(sqUtilisateur).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun completerSideQuest(userId: String, questId: String): Result<Unit> {
        val docId = "${userId}_$questId"
        return try {
            db.collection(COLLECTION_SQ_UTILISATEURS).document(docId).update(
                mapOf(
                    "completee" to true,
                    "dateCompletion" to System.currentTimeMillis()
                )
            ).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
