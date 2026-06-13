package com.example.fitrack.repository.firestore

import com.example.fitrack.model.Invitation
import com.example.fitrack.model.ProfilPublic
import com.example.fitrack.repository.SocialRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirestoreSocialRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : SocialRepository {

    companion object {
        private const val COLLECTION  = "profilsPublics"
        private const val INVITATIONS = "invitations"
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

    override fun observerClassement(): Flow<List<ProfilPublic>> = callbackFlow {
        val listener = db.collection(COLLECTION)
            .orderBy("scoreHebdo", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) { close(error); return@addSnapshotListener }
                val profils = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ProfilPublic::class.java)?.copy(userId = doc.id)
                } ?: emptyList()
                trySend(profils)
            }
        awaitClose { listener.remove() }
    }

    override suspend fun lireProfilPublic(userId: String): Result<ProfilPublic> = try {
        val doc = db.collection(COLLECTION).document(userId).get().await()
        val profil = doc.toObject(ProfilPublic::class.java)?.copy(userId = doc.id)
        if (profil != null) Result.success(profil)
        else Result.failure(Exception("Profil introuvable"))
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

    override suspend fun envoyerInvitation(invitation: Invitation): Result<Unit> = try {
        val ref = db.collection(INVITATIONS).document()
        db.collection(INVITATIONS).document(ref.id)
            .set(invitation.copy(id = ref.id, timestamp = System.currentTimeMillis()))
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun mesInvitationsRecues(userId: String): Result<List<Invitation>> = try {
        val snapshot = db.collection(INVITATIONS)
            .whereEqualTo("destinataireId", userId)
            .whereEqualTo("statut", Invitation.StatutInvitation.EN_ATTENTE)
            .get().await()
        Result.success(snapshot.documents.mapNotNull { it.toObject(Invitation::class.java) })
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun mesInvitationsEnvoyees(userId: String): Result<List<Invitation>> = try {
        val snapshot = db.collection(INVITATIONS)
            .whereEqualTo("envoyeurId", userId)
            .get().await()
        Result.success(snapshot.documents.mapNotNull { it.toObject(Invitation::class.java) })
    } catch (e: Exception) {
        Result.failure(e)
    }

    override suspend fun repondreInvitation(invitationId: String, accepte: Boolean): Result<Unit> = try {
        val statut = if (accepte) Invitation.StatutInvitation.ACCEPTEE
                     else Invitation.StatutInvitation.REFUSEE
        db.collection(INVITATIONS).document(invitationId)
            .update("statut", statut)
            .await()
        Result.success(Unit)
    } catch (e: Exception) {
        Result.failure(e)
    }
}
