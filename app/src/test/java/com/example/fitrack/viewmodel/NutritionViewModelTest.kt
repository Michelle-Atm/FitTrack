package com.example.fitrack.viewmodel

import com.example.fitrack.fakes.FakeNutritionRepository
import com.example.fitrack.fakes.FakeObjectifRepository
import com.example.fitrack.model.AlimentOFF
import com.example.fitrack.model.Objectif
import com.example.fitrack.model.Repas
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.Calendar

@OptIn(ExperimentalCoroutinesApi::class)
class NutritionViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeNutritionRepository
    private lateinit var objectifRepo: FakeObjectifRepository
    private lateinit var viewModel: NutritionViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeNutritionRepository()
        objectifRepo = FakeObjectifRepository()
        viewModel = NutritionViewModel(repo, objectifRepo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- calculerTotaux ---

    @Test
    fun `calculerTotaux retourne zeros pour liste vide`() {
        val totaux = viewModel.calculerTotaux(emptyList())
        assertEquals(0.0, totaux.calories, 0.0)
        assertEquals(0.0, totaux.proteines, 0.0)
        assertEquals(0.0, totaux.glucides, 0.0)
        assertEquals(0.0, totaux.lipides, 0.0)
        assertEquals(0.0, totaux.fibres, 0.0)
    }

    @Test
    fun `calculerTotaux additionne correctement un seul repas`() {
        val repas = listOf(Repas(calories = 400.0, proteines = 30.0, glucides = 50.0, lipides = 10.0, fibres = 5.0))
        val totaux = viewModel.calculerTotaux(repas)
        assertEquals(400.0, totaux.calories, 0.0)
        assertEquals(30.0, totaux.proteines, 0.0)
        assertEquals(50.0, totaux.glucides, 0.0)
        assertEquals(10.0, totaux.lipides, 0.0)
        assertEquals(5.0, totaux.fibres, 0.0)
    }

    @Test
    fun `calculerTotaux additionne plusieurs repas`() {
        val repas = listOf(
            Repas(calories = 300.0, proteines = 20.0, glucides = 40.0, lipides = 8.0, fibres = 3.0),
            Repas(calories = 500.0, proteines = 35.0, glucides = 60.0, lipides = 15.0, fibres = 7.0),
            Repas(calories = 200.0, proteines = 10.0, glucides = 30.0, lipides = 5.0, fibres = 2.0)
        )
        val totaux = viewModel.calculerTotaux(repas)
        assertEquals(1000.0, totaux.calories, 0.0)
        assertEquals(65.0, totaux.proteines, 0.0)
        assertEquals(130.0, totaux.glucides, 0.0)
        assertEquals(28.0, totaux.lipides, 0.0)
        assertEquals(12.0, totaux.fibres, 0.0)
    }

    // --- calculerComparaison ---

    @Test
    fun `calculerComparaison retourne pourcentages corrects`() {
        val totaux = NutritionViewModel.TotauxJournaliers(calories = 1500.0, proteines = 75.0, glucides = 200.0, lipides = 50.0)
        val objectif = Objectif(caloriesObjectif = 2000.0, proteinesObjectif = 150.0, glucidesObjectif = 250.0, lipidesObjectif = 65.0)
        val comp = viewModel.calculerComparaison(totaux, objectif)
        assertEquals(0.75f, comp.pourcentageCalories, 0.001f)
        assertEquals(0.5f, comp.pourcentageProteines, 0.001f)
        assertEquals(0.8f, comp.pourcentageGlucides, 0.001f)
    }

    @Test
    fun `calculerComparaison retourne restantes correctes`() {
        val totaux = NutritionViewModel.TotauxJournaliers(calories = 800.0, proteines = 60.0, glucides = 100.0, lipides = 30.0)
        val objectif = Objectif(caloriesObjectif = 2000.0, proteinesObjectif = 150.0, glucidesObjectif = 250.0, lipidesObjectif = 65.0)
        val comp = viewModel.calculerComparaison(totaux, objectif)
        assertEquals(1200.0, comp.caloriesRestantes, 0.001)
        assertEquals(90.0, comp.proteinesRestantes, 0.001)
        assertEquals(150.0, comp.glucidesRestantes, 0.001)
        assertEquals(35.0, comp.lipidesRestantes, 0.001)
    }

    @Test
    fun `calculerComparaison retourne restantes negatives si depassement`() {
        val totaux = NutritionViewModel.TotauxJournaliers(calories = 2500.0, proteines = 0.0, glucides = 0.0, lipides = 0.0)
        val objectif = Objectif(caloriesObjectif = 2000.0)
        val comp = viewModel.calculerComparaison(totaux, objectif)
        assertEquals(-500.0, comp.caloriesRestantes, 0.001)
        assertTrue(comp.pourcentageCalories > 1.0f)
    }

    @Test
    fun `calculerComparaison retourne zero si objectif a zero`() {
        val totaux = NutritionViewModel.TotauxJournaliers(calories = 500.0, proteines = 30.0, glucides = 0.0, lipides = 0.0)
        val objectif = Objectif(caloriesObjectif = 0.0, proteinesObjectif = 0.0)
        val comp = viewModel.calculerComparaison(totaux, objectif)
        assertEquals(0f, comp.pourcentageCalories, 0.0f)
        assertEquals(0f, comp.pourcentageProteines, 0.0f)
    }

    // --- debutJournee ---

    @Test
    fun `debutJournee retourne minuit du jour donne`() {
        val cal = Calendar.getInstance().apply {
            set(2026, Calendar.APRIL, 19, 14, 30, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val timestamp = cal.timeInMillis
        val debut = viewModel.debutJournee(timestamp)
        val resultat = Calendar.getInstance().apply { timeInMillis = debut }

        assertEquals(0, resultat.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, resultat.get(Calendar.MINUTE))
        assertEquals(0, resultat.get(Calendar.SECOND))
        assertEquals(0, resultat.get(Calendar.MILLISECOND))
        assertEquals(19, resultat.get(Calendar.DAY_OF_MONTH))
        assertEquals(Calendar.APRIL, resultat.get(Calendar.MONTH))
    }

    @Test
    fun `debutJournee sans argument retourne minuit du jour courant`() {
        val debut = viewModel.debutJournee()
        val cal = Calendar.getInstance().apply { timeInMillis = debut }
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, cal.get(Calendar.MINUTE))
        assertEquals(0, cal.get(Calendar.SECOND))
    }

    // --- rechercherAliment ---

    @Test
    fun `rechercherAliment query vide passe a Idle sans appel repo`() {
        viewModel.rechercherAliment("")
        assertTrue(viewModel.rechercheState.value is NutritionViewModel.RechercheState.Idle)
    }

    @Test
    fun `rechercherAliment query blanche passe a Idle`() {
        viewModel.rechercherAliment("   ")
        assertTrue(viewModel.rechercheState.value is NutritionViewModel.RechercheState.Idle)
    }

    @Test
    fun `rechercherAliment succes expose resultats`() = runTest {
        val aliments = listOf(AlimentOFF(code = "123", nom = "Pomme", calories = 52.0))
        repo.rechercherAlimentResult = Result.success(aliments)
        viewModel.rechercherAliment("pomme")
        advanceUntilIdle()
        val state = viewModel.rechercheState.value
        assertTrue(state is NutritionViewModel.RechercheState.Resultats)
        assertEquals(1, (state as NutritionViewModel.RechercheState.Resultats).aliments.size)
        assertEquals("Pomme", state.aliments[0].nom)
    }

    @Test
    fun `rechercherAliment echec expose erreur`() = runTest {
        repo.rechercherAlimentResult = Result.failure(Exception("Réseau indisponible"))
        viewModel.rechercherAliment("poulet")
        advanceUntilIdle()
        val state = viewModel.rechercheState.value
        assertTrue(state is NutritionViewModel.RechercheState.Erreur)
        assertEquals("Réseau indisponible", (state as NutritionViewModel.RechercheState.Erreur).message)
    }

    @Test
    fun `rechercherParCodeBarres produit trouve expose resultats`() = runTest {
        val aliment = AlimentOFF(code = "3017620422003", nom = "Nutella", calories = 539.0)
        repo.rechercherCodeBarresResult = Result.success(aliment)
        viewModel.rechercherParCodeBarres("3017620422003")
        advanceUntilIdle()
        val state = viewModel.rechercheState.value
        assertTrue(state is NutritionViewModel.RechercheState.Resultats)
        assertEquals("Nutella", (state as NutritionViewModel.RechercheState.Resultats).aliments[0].nom)
    }

    @Test
    fun `rechercherParCodeBarres produit introuvable expose erreur`() = runTest {
        repo.rechercherCodeBarresResult = Result.success(null)
        viewModel.rechercherParCodeBarres("9999999999999")
        advanceUntilIdle()
        assertTrue(viewModel.rechercheState.value is NutritionViewModel.RechercheState.Erreur)
    }

    // --- chargerRepasJournaliers ---

    @Test
    fun `chargerRepasJournaliers succes expose repas et totaux`() = runTest {
        val repas = listOf(
            Repas(id = "1", userId = "u1", calories = 600.0, proteines = 40.0, glucides = 80.0, lipides = 20.0, fibres = 5.0)
        )
        repo.repasJournalierResult = Result.success(repas)
        viewModel.chargerRepasJournaliers("u1")
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is NutritionViewModel.NutritionUiState.Succes)
        val succes = state as NutritionViewModel.NutritionUiState.Succes
        assertEquals(1, succes.repas.size)
        assertEquals(600.0, succes.totaux.calories, 0.0)
    }

    @Test
    fun `chargerRepasJournaliers expose comparaison avec objectif`() = runTest {
        val repas = listOf(Repas(calories = 1200.0, proteines = 60.0, glucides = 150.0, lipides = 40.0))
        repo.repasJournalierResult = Result.success(repas)
        objectifRepo.objectifJournalierResult = Result.success(
            Objectif(caloriesObjectif = 2000.0, proteinesObjectif = 150.0, glucidesObjectif = 250.0, lipidesObjectif = 65.0)
        )
        viewModel.chargerRepasJournaliers("u1")
        advanceUntilIdle()
        val succes = viewModel.uiState.value as NutritionViewModel.NutritionUiState.Succes
        assertEquals(0.6f, succes.comparaison.pourcentageCalories, 0.001f)
        assertEquals(800.0, succes.comparaison.caloriesRestantes, 0.001)
    }

    @Test
    fun `chargerRepasJournaliers comparaison vide si objectif absent`() = runTest {
        repo.repasJournalierResult = Result.success(listOf(Repas(calories = 600.0)))
        objectifRepo.objectifJournalierResult = Result.failure(Exception("Pas d'objectif"))
        viewModel.chargerRepasJournaliers("u1")
        advanceUntilIdle()
        val succes = viewModel.uiState.value as NutritionViewModel.NutritionUiState.Succes
        assertEquals(0f, succes.comparaison.pourcentageCalories)
        assertEquals(0.0, succes.comparaison.caloriesRestantes, 0.0)
    }

    @Test
    fun `chargerRepasJournaliers echec expose erreur`() = runTest {
        repo.repasJournalierResult = Result.failure(Exception("Firestore indisponible"))
        viewModel.chargerRepasJournaliers("u1")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is NutritionViewModel.NutritionUiState.Erreur)
    }

    // --- reinitialiserRecherche ---

    @Test
    fun `reinitialiserRecherche remet rechercheState a Idle`() = runTest {
        val aliments = listOf(AlimentOFF(nom = "Pomme"))
        repo.rechercherAlimentResult = Result.success(aliments)
        viewModel.rechercherAliment("pomme")
        advanceUntilIdle()
        viewModel.reinitialiserRecherche()
        assertTrue(viewModel.rechercheState.value is NutritionViewModel.RechercheState.Idle)
    }
}
