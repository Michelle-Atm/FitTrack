package com.example.fitrack.repository.firestore

import android.util.Log
import com.example.fitrack.model.AlimentOFF
import com.example.fitrack.model.Repas
import com.example.fitrack.repository.NutritionRepository
import com.example.fitrack.repository.api.OFFProduct
import com.example.fitrack.repository.api.OpenFoodFactsApiService
import com.example.fitrack.repository.api.RetrofitClient
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
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
        Log.d("FitTrackRepas", "ajouterRepas - nom: ${repas.nom}, userId: ${repas.userId}, date: ${repas.date}")
        return try {
            val ref = db.collection(COLLECTION_REPAS).document()
            db.collection(COLLECTION_REPAS).document(ref.id).set(repas.copy(id = ref.id)).await()
            Log.d("FitTrackRepas", "ajouterRepas success - docId: ${ref.id}")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("FitTrackRepas", "ajouterRepas error", e)
            Result.failure(e)
        }
    }

    override suspend fun repasJournalier(userId: String, dateDebut: Long, dateFin: Long): Result<List<Repas>> {
        Log.d("FitTrackRepas", "repasJournalier query - userId: $userId, dateDebut: $dateDebut, dateFin: $dateFin")
        return try {
            val snapshot = db.collection(COLLECTION_REPAS)
                .whereEqualTo("userId", userId)
                .get().await()
            val repas = snapshot.toObjects(Repas::class.java)
                .filter { it.date in dateDebut until dateFin }
                .sortedBy { it.date }
            Log.d("FitTrackRepas", "repasJournalier success - size: ${repas.size}")
            for (r in repas) {
                Log.d("FitTrackRepas", "  repas: ${r.nom}, date: ${r.date}")
            }
            Result.success(repas)
        } catch (e: FirebaseFirestoreException) {
            Log.e("FitTrackRepas", "repasJournalier firestore error", e)
            when (e.code) {
                FirebaseFirestoreException.Code.UNAVAILABLE,
                FirebaseFirestoreException.Code.NOT_FOUND -> Result.success(emptyList())
                else -> Result.failure(e)
            }
        } catch (e: Exception) {
            Log.e("FitTrackRepas", "repasJournalier exception", e)
            Result.failure(e)
        }
    }

    override suspend fun historiqueRepas(userId: String, joursEnArriere: Int): Result<List<Repas>> {
        val dateDebut = System.currentTimeMillis() - TimeUnit.DAYS.toMillis(joursEnArriere.toLong())
        Log.d("FitTrackRepas", "historiqueRepas query - userId: $userId, dateDebut: $dateDebut")
        return try {
            val snapshot = db.collection(COLLECTION_REPAS)
                .whereEqualTo("userId", userId)
                .get().await()
            val repas = snapshot.toObjects(Repas::class.java)
                .filter { it.date >= dateDebut }
                .sortedByDescending { it.date }
            Log.d("FitTrackRepas", "historiqueRepas success - size: ${repas.size}")
            Result.success(repas)
        } catch (e: FirebaseFirestoreException) {
            Log.e("FitTrackRepas", "historiqueRepas firestore error", e)
            when (e.code) {
                FirebaseFirestoreException.Code.UNAVAILABLE,
                FirebaseFirestoreException.Code.NOT_FOUND -> Result.success(emptyList())
                else -> Result.failure(e)
            }
        } catch (e: Exception) {
            Log.e("FitTrackRepas", "historiqueRepas exception", e)
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
        calories = nutriments?.calories ?: 0.0,
        proteines = nutriments?.proteines ?: 0.0,
        glucides = nutriments?.glucides ?: 0.0,
        lipides = nutriments?.lipides ?: 0.0,
        fibres = nutriments?.fibres ?: 0.0,
        imageUrl = imageUrlEffective,
        allergenes = allergenes.orEmpty()
    )
}
