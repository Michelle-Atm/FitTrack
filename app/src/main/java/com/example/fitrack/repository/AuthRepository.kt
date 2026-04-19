package com.example.fitrack.repository

import com.example.fitrack.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observerUtilisateur(): Flow<User?>
    suspend fun connexion(email: String, motDePasse: String): Result<User>
    suspend fun inscription(email: String, motDePasse: String, user: User): Result<User>
    suspend fun deconnexion()
    suspend fun mettreAJourProfil(user: User): Result<Unit>
    suspend fun recupererProfil(uid: String): Result<User>
}
