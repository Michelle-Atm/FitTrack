package com.example.fitrack.repository

import com.example.fitrack.model.Objectif
import com.example.fitrack.model.Seance
import com.example.fitrack.model.SideQuest
import com.example.fitrack.model.SideQuestUtilisateur

interface ObjectifRepository {
    suspend fun objectifJournalier(userId: String, date: Long): Result<Objectif>
    suspend fun mettreAJourObjectif(objectif: Objectif): Result<Unit>
    suspend fun ajouterSeance(seance: Seance): Result<Unit>
    suspend fun seancesUtilisateur(userId: String, dateDebut: Long, dateFin: Long): Result<List<Seance>>
    suspend fun sideQuestsDisponibles(): Result<List<SideQuest>>
    suspend fun sideQuestsUtilisateur(userId: String): Result<List<SideQuestUtilisateur>>
    suspend fun debloquerSideQuest(userId: String, questId: String): Result<Unit>
    suspend fun completerSideQuest(userId: String, questId: String): Result<Unit>
}
