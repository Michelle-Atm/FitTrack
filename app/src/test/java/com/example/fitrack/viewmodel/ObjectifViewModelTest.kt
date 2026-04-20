package com.example.fitrack.viewmodel

import com.example.fitrack.fakes.FakeObjectifRepository
import com.example.fitrack.model.Objectif
import com.example.fitrack.model.Seance
import com.example.fitrack.model.SideQuest
import com.example.fitrack.model.SideQuestUtilisateur
import com.example.fitrack.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ObjectifViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeObjectifRepository
    private lateinit var viewModel: ObjectifViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeObjectifRepository()
        viewModel = ObjectifViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- calculerProgression ---

    @Test
    fun `calculerProgression retourne 0 si cible a zero`() {
        val objectif = Objectif(caloriesObjectif = 0.0, caloriesActuelles = 500.0)
        val progression = viewModel.calculerProgression(objectif)
        assertEquals(0f, progression.progressionCalories)
    }

    @Test
    fun `calculerProgression retourne 1 si objectif atteint`() {
        val objectif = Objectif(
            caloriesObjectif = 2000.0, caloriesActuelles = 2000.0,
            proteinesObjectif = 150.0, proteinesActuelles = 150.0
        )
        val progression = viewModel.calculerProgression(objectif)
        assertEquals(1f, progression.progressionCalories)
        assertEquals(1f, progression.progressionProteines)
    }

    @Test
    fun `calculerProgression est plafonnee a 1`() {
        val objectif = Objectif(caloriesObjectif = 2000.0, caloriesActuelles = 3000.0)
        val progression = viewModel.calculerProgression(objectif)
        assertEquals(1f, progression.progressionCalories)
    }

    @Test
    fun `calculerProgression objectifsDepasseCalories a 110 pourcent`() {
        val objectif = Objectif(caloriesObjectif = 2000.0, caloriesActuelles = 2201.0)
        val progression = viewModel.calculerProgression(objectif)
        assertTrue(progression.objectifsDepasseCalories)
    }

    @Test
    fun `calculerProgression objectifsDepasseCalories faux si juste en dessous`() {
        val objectif = Objectif(caloriesObjectif = 2000.0, caloriesActuelles = 2199.0)
        val progression = viewModel.calculerProgression(objectif)
        assertFalse(progression.objectifsDepasseCalories)
    }

    @Test
    fun `calculerProgression progression seances correcte`() {
        val objectif = Objectif(seancesObjectif = 4, seancesEffectuees = 2)
        val progression = viewModel.calculerProgression(objectif)
        assertEquals(0.5f, progression.progressionSeances)
    }

    @Test
    fun `calculerProgression progression pas correcte`() {
        val objectif = Objectif(pasObjectif = 10000, pasActuels = 7500)
        val progression = viewModel.calculerProgression(objectif)
        assertEquals(0.75f, progression.progressionPas)
    }

    // --- objectifAtteint ---

    @Test
    fun `objectifAtteint vrai si tous les seuils a 90 pourcent`() {
        val objectif = Objectif(
            caloriesObjectif = 2000.0, caloriesActuelles = 1800.0,
            proteinesObjectif = 150.0, proteinesActuelles = 135.0,
            seancesObjectif = 3, seancesEffectuees = 3
        )
        assertTrue(viewModel.objectifAtteint(objectif))
    }

    @Test
    fun `objectifAtteint faux si calories insuffisantes`() {
        val objectif = Objectif(
            caloriesObjectif = 2000.0, caloriesActuelles = 1000.0,
            proteinesObjectif = 150.0, proteinesActuelles = 150.0,
            seancesObjectif = 1, seancesEffectuees = 1
        )
        assertFalse(viewModel.objectifAtteint(objectif))
    }

    @Test
    fun `objectifAtteint faux si seances insuffisantes`() {
        val objectif = Objectif(
            caloriesObjectif = 2000.0, caloriesActuelles = 2000.0,
            proteinesObjectif = 150.0, proteinesActuelles = 150.0,
            seancesObjectif = 3, seancesEffectuees = 2
        )
        assertFalse(viewModel.objectifAtteint(objectif))
    }

    // --- conditionDeblocageRemplie ---

    @Test
    fun `conditionDeblocageRemplie niveau atteint`() {
        val user = User(niveau = 10)
        assertTrue(viewModel.conditionDeblocageRemplie(user, "niveau_5"))
    }

    @Test
    fun `conditionDeblocageRemplie niveau non atteint`() {
        val user = User(niveau = 3)
        assertFalse(viewModel.conditionDeblocageRemplie(user, "niveau_5"))
    }

    @Test
    fun `conditionDeblocageRemplie niveau exact`() {
        val user = User(niveau = 5)
        assertTrue(viewModel.conditionDeblocageRemplie(user, "niveau_5"))
    }

    @Test
    fun `conditionDeblocageRemplie xp atteint`() {
        val user = User(xp = 1000)
        assertTrue(viewModel.conditionDeblocageRemplie(user, "xp_500"))
    }

    @Test
    fun `conditionDeblocageRemplie xp insuffisant`() {
        val user = User(xp = 200)
        assertFalse(viewModel.conditionDeblocageRemplie(user, "xp_500"))
    }

    @Test
    fun `conditionDeblocageRemplie condition inconnue retourne faux`() {
        val user = User(niveau = 99, xp = 99999)
        assertFalse(viewModel.conditionDeblocageRemplie(user, "condition_inconnue"))
    }

    // --- chargerObjectifJournalier ---

    @Test
    fun `chargerObjectifJournalier succes expose progression`() = runTest {
        val objectif = Objectif(
            id = "u1_123",
            userId = "u1",
            caloriesObjectif = 2000.0,
            caloriesActuelles = 1200.0
        )
        repo.objectifJournalierResult = Result.success(objectif)
        viewModel.chargerObjectifJournalier("u1")
        advanceUntilIdle()
        val state = viewModel.objectifUiState.value
        assertTrue(state is ObjectifViewModel.ObjectifUiState.Succes)
        assertEquals(0.6f, (state as ObjectifViewModel.ObjectifUiState.Succes).progression.progressionCalories, 0.01f)
    }

    @Test
    fun `chargerObjectifJournalier echec expose erreur`() = runTest {
        repo.objectifJournalierResult = Result.failure(Exception("Firestore hors ligne"))
        viewModel.chargerObjectifJournalier("u1")
        advanceUntilIdle()
        assertTrue(viewModel.objectifUiState.value is ObjectifViewModel.ObjectifUiState.Erreur)
    }

    // --- chargerSideQuests ---

    @Test
    fun `chargerSideQuests succes expose listes`() = runTest {
        repo.sideQuestsDisponiblesResult = Result.success(listOf(SideQuest(id = "sq1", titre = "Courir 5km")))
        repo.sideQuestsUtilisateurResult = Result.success(listOf(SideQuestUtilisateur(userId = "u1", questId = "sq1")))
        viewModel.chargerSideQuests("u1")
        advanceUntilIdle()
        val state = viewModel.sideQuestUiState.value
        assertTrue(state is ObjectifViewModel.SideQuestUiState.Succes)
        assertEquals(1, (state as ObjectifViewModel.SideQuestUiState.Succes).disponibles.size)
        assertEquals(1, state.utilisateur.size)
    }

    @Test
    fun `chargerSideQuests echec disponibles expose erreur`() = runTest {
        repo.sideQuestsDisponiblesResult = Result.failure(Exception("Erreur Firestore"))
        viewModel.chargerSideQuests("u1")
        advanceUntilIdle()
        assertTrue(viewModel.sideQuestUiState.value is ObjectifViewModel.SideQuestUiState.Erreur)
    }

    // --- loggerSeance : incrémente seancesEffectuees via Firestore ---

    @Test
    fun `loggerSeance incremente seancesEffectuees depuis Firestore`() = runTest {
        val objectifInitial = Objectif(id = "u1_0", userId = "u1", seancesObjectif = 3, seancesEffectuees = 1)
        repo.objectifJournalierResult = Result.success(objectifInitial)
        repo.ajouterSeanceResult = Result.success(Unit)

        viewModel.loggerSeance(Seance(dureeMinutes = 30, type = "cardio"), "u1")
        advanceUntilIdle()

        assertEquals(1, repo.objectifsMisAJour.size)
        assertEquals(2, repo.objectifsMisAJour[0].seancesEffectuees)
    }
}
