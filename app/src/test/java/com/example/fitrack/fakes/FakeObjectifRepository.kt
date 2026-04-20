package com.example.fitrack.fakes

import com.example.fitrack.model.Objectif
import com.example.fitrack.model.Seance
import com.example.fitrack.model.SideQuest
import com.example.fitrack.model.SideQuestUtilisateur
import com.example.fitrack.repository.ObjectifRepository

class FakeObjectifRepository : ObjectifRepository {
    var objectifJournalierResult: Result<Objectif> = Result.success(Objectif())
    var mettreAJourResult: Result<Unit> = Result.success(Unit)
    var ajouterSeanceResult: Result<Unit> = Result.success(Unit)
    var seancesResult: Result<List<Seance>> = Result.success(emptyList())
    var sideQuestsDisponiblesResult: Result<List<SideQuest>> = Result.success(emptyList())
    var sideQuestsUtilisateurResult: Result<List<SideQuestUtilisateur>> = Result.success(emptyList())
    var debloquerResult: Result<Unit> = Result.success(Unit)
    var completerResult: Result<Unit> = Result.success(Unit)

    val objectifsMisAJour = mutableListOf<Objectif>()

    override suspend fun objectifJournalier(userId: String, date: Long) = objectifJournalierResult
    override suspend fun mettreAJourObjectif(objectif: Objectif): Result<Unit> {
        objectifsMisAJour.add(objectif)
        return mettreAJourResult
    }
    override suspend fun ajouterSeance(seance: Seance) = ajouterSeanceResult
    override suspend fun seancesUtilisateur(userId: String, dateDebut: Long, dateFin: Long) = seancesResult
    override suspend fun sideQuestsDisponibles() = sideQuestsDisponiblesResult
    override suspend fun sideQuestsUtilisateur(userId: String) = sideQuestsUtilisateurResult
    override suspend fun debloquerSideQuest(userId: String, questId: String) = debloquerResult
    override suspend fun completerSideQuest(userId: String, questId: String) = completerResult
}
