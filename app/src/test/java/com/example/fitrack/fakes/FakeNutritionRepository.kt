package com.example.fitrack.fakes

import com.example.fitrack.model.AlimentOFF
import com.example.fitrack.model.Repas
import com.example.fitrack.repository.NutritionRepository

class FakeNutritionRepository : NutritionRepository {
    var repasJournalierResult: Result<List<Repas>> = Result.success(emptyList())
    var historiqueResult: Result<List<Repas>> = Result.success(emptyList())
    var ajouterRepasResult: Result<Unit> = Result.success(Unit)
    var supprimerRepasResult: Result<Unit> = Result.success(Unit)
    var rechercherAlimentResult: Result<List<AlimentOFF>> = Result.success(emptyList())
    var rechercherCodeBarresResult: Result<AlimentOFF?> = Result.success(null)

    override suspend fun ajouterRepas(repas: Repas) = ajouterRepasResult
    override suspend fun repasJournalier(userId: String, dateDebut: Long, dateFin: Long) = repasJournalierResult
    override suspend fun historiqueRepas(userId: String, joursEnArriere: Int) = historiqueResult
    override suspend fun supprimerRepas(repasId: String) = supprimerRepasResult
    override suspend fun rechercherAliment(query: String) = rechercherAlimentResult
    override suspend fun rechercherParCodeBarres(code: String) = rechercherCodeBarresResult
}
