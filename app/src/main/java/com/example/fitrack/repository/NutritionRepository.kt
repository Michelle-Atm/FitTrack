package com.example.fitrack.repository

import com.example.fitrack.model.AlimentOFF
import com.example.fitrack.model.Repas

interface NutritionRepository {
    suspend fun ajouterRepas(repas: Repas): Result<Unit>
    suspend fun repasJournalier(userId: String, dateDebut: Long, dateFin: Long): Result<List<Repas>>
    suspend fun historiqueRepas(userId: String, joursEnArriere: Int): Result<List<Repas>>
    suspend fun supprimerRepas(repasId: String): Result<Unit>
    suspend fun rechercherAliment(query: String): Result<List<AlimentOFF>>
    suspend fun rechercherParCodeBarres(code: String): Result<AlimentOFF?>
}
