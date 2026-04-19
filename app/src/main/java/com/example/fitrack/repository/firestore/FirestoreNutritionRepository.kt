package com.example.fitrack.repository.firestore

import com.example.fitrack.model.AlimentOFF
import com.example.fitrack.model.Repas
import com.example.fitrack.repository.NutritionRepository
import com.example.fitrack.repository.api.OFFProduct
import com.example.fitrack.repository.api.OpenFoodFactsApiService
import com.example.fitrack.repository.api.RetrofitClient
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Calendar
import java.util.concurrent.TimeUnit

class FirestoreNutritionRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val offService: OpenFoodFactsApiService = RetrofitClient.openFoodFactsService
) : NutritionRepository {

    companion object {
        private const val COLLECTION_REPAS = "repas"
    }

    override suspend fun ajouterRepas(repas: Repas): Result<Unit> {
        return try {
            val ref = db.collection(COLLECTION_REPAS).document()
            db.collection(COLLECTION_REPAS).document(ref.id).set(repas.copy(id = ref.id)).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun repasJournalier(userId: String, dateDebut: Long, dateFin: Long): Result<List<Repas>> {
        return try {
            val snapshot = db.collection(COLLECTION_REPAS)
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", dateDebut)
                .whereLessThan("date", dateFin)
                .orderBy("date", Query.Direction.ASCENDING)
                .get().await()
            Result.success(snapshot.toObjects(Repas::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun historiqueRepas(userId: String, joursEnArriere: Int): Result<List<Repas>> {
        val dateDebut = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(joursEnArriere.toLong())
        return try {
            val snapshot = db.collection(COLLECTION_REPAS)
                .whereEqualTo("userId", userId)
                .whereGreaterThanOrEqualTo("date", dateDebut)
                .orderBy("date", Query.Direction.DESCENDING)
                .get().await()
            Result.success(snapshot.toObjects(Repas::class.java))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun supprimerRepas(repasId: String): Result<Unit> {
        return try {
            db.collection(COLLECTION_REPAS).document(repasId).delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun rechercherAliment(query: String): Result<List<AlimentOFF>> {
        return try {
            val response = offService.rechercherAliments(terme = query)
            if (response.isSuccessful) {
                val aliments = response.body()?.products?.map { it.toDomaine() } ?: emptyList()
                Result.success(aliments)
            } else {
                Result.failure(Exception("Erreur API: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun rechercherParCodeBarres(code: String): Result<AlimentOFF?> {
        return try {
            val response = offService.produitParCodeBarres(barcode = code)
            if (response.isSuccessful && response.body()?.status == 1) {
                Result.success(response.body()?.product?.toDomaine())
            } else {
                Result.success(null)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun OFFProduct.toDomaine() = AlimentOFF(
        code = code,
        nom = nom,
        calories = nutriments.calories,
        proteines = nutriments.proteines,
        glucides = nutriments.glucides,
        lipides = nutriments.lipides,
        fibres = nutriments.fibres,
        imageUrl = imageUrl
    )
}
