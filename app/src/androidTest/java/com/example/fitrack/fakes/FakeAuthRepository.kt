package com.example.fitrack.fakes

import com.example.fitrack.model.User
import com.example.fitrack.repository.AuthRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeAuthRepository : AuthRepository {
    var connexionResult: Result<User> = Result.success(User(uid = "uid1", email = "test@test.com", nom = "Test"))
    var inscriptionResult: Result<User> = Result.success(User(uid = "uid1", email = "test@test.com", nom = "Test"))
    var mettreAJourResult: Result<Unit> = Result.success(Unit)
    var recupererResult: Result<User> = Result.success(User(uid = "uid1"))
    var utilisateurObserve: User? = null

    override fun observerUtilisateur(): Flow<User?> = flowOf(utilisateurObserve)
    override suspend fun connexion(email: String, motDePasse: String) = connexionResult
    override suspend fun inscription(email: String, motDePasse: String, user: User) = inscriptionResult
    override suspend fun deconnexion() {}
    override suspend fun mettreAJourProfil(user: User) = mettreAJourResult
    override suspend fun recupererProfil(uid: String) = recupererResult
}
