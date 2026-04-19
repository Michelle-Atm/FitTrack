package com.example.fitrack.repository.firestore

import com.example.fitrack.model.User
import com.example.fitrack.repository.AuthRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

// Nécessite google-services.json pour s'initialiser au runtime.
class FirestoreAuthRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AuthRepository {

    companion object {
        private const val COLLECTION_USERS = "users"
    }

    override fun observerUtilisateur(): Flow<User?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            val firebaseUser = firebaseAuth.currentUser
            if (firebaseUser == null) {
                trySend(null)
            } else {
                trySend(User(uid = firebaseUser.uid, email = firebaseUser.email ?: ""))
            }
        }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun connexion(email: String, motDePasse: String): Result<User> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, motDePasse).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("UID introuvable"))
            recupererProfil(uid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun inscription(email: String, motDePasse: String, user: User): Result<User> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, motDePasse).await()
            val uid = result.user?.uid ?: return Result.failure(Exception("UID introuvable"))
            val userAvecUid = user.copy(
                uid = uid,
                email = email,
                dateCreation = System.currentTimeMillis()
            )
            db.collection(COLLECTION_USERS).document(uid).set(userAvecUid).await()
            Result.success(userAvecUid)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deconnexion() {
        auth.signOut()
    }

    override suspend fun mettreAJourProfil(user: User): Result<Unit> {
        return try {
            db.collection(COLLECTION_USERS).document(user.uid).set(user).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun recupererProfil(uid: String): Result<User> {
        return try {
            val doc = db.collection(COLLECTION_USERS).document(uid).get().await()
            val user = doc.toObject(User::class.java)
                ?: return Result.failure(Exception("Profil introuvable pour uid=$uid"))
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
