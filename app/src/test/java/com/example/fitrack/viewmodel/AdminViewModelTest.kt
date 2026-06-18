package com.example.fitrack.viewmodel

import com.example.fitrack.fakes.FakeAdminRepository
import com.example.fitrack.model.ScorePalier
import com.example.fitrack.model.User
import com.example.fitrack.repository.StatsGlobales
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
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
class AdminViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeAdminRepository
    private lateinit var viewModel: AdminViewModel

    private val userA = User(uid = "u1", nom = "Alice", email = "alice@test.com", xp = 100, niveau = 2)
    private val userAdmin = User(uid = "uAdmin", nom = "Boss", email = "boss@test.com", isAdmin = true)
    private val userSuspendu = User(uid = "u2", nom = "Bob", email = "bob@test.com", suspendu = true)
    private val palier1 = ScorePalier("p1", 1, "Débutant", 0, 299)
    private val palier2 = ScorePalier("p2", 2, "Expert", 300, Int.MAX_VALUE)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeAdminRepository()
        viewModel = AdminViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ── chargerTousLesUtilisateurs ────────────────────────────────────────────

    @Test
    fun `chargerTousLesUtilisateurs - succes remplit la liste`() = runTest {
        repo.utilisateurs = mutableListOf(userA, userAdmin)
        viewModel.chargerTousLesUtilisateurs()
        advanceUntilIdle()
        val state = viewModel.utilisateursState.value as AdminViewModel.UtilisateursState.Succes
        assertEquals(2, state.utilisateurs.size)
    }

    @Test
    fun `chargerTousLesUtilisateurs - erreur reseau produit etat Erreur`() = runTest {
        repo.lireUtilisateursResult = Result.failure(Exception("timeout"))
        viewModel.chargerTousLesUtilisateurs()
        advanceUntilIdle()
        val state = viewModel.utilisateursState.value
        assertTrue(state is AdminViewModel.UtilisateursState.Erreur)
    }

    // ── supprimerUtilisateur ──────────────────────────────────────────────────

    @Test
    fun `supprimerUtilisateur - retire l utilisateur de la liste locale`() = runTest {
        repo.utilisateurs = mutableListOf(userA, userAdmin)
        viewModel.chargerTousLesUtilisateurs()
        advanceUntilIdle()

        viewModel.supprimerUtilisateur(userA.uid)
        advanceUntilIdle()

        val state = viewModel.utilisateursState.value as AdminViewModel.UtilisateursState.Succes
        assertFalse(state.utilisateurs.any { it.uid == userA.uid })
        assertEquals(listOf(userA.uid), repo.supprimerAppels)
    }

    @Test
    fun `supprimerUtilisateur - echec emet evenement d erreur`() = runTest {
        repo.utilisateurs = mutableListOf(userA)
        viewModel.chargerTousLesUtilisateurs()
        advanceUntilIdle()
        repo.supprimerResult = Result.failure(Exception("permission denied"))

        val evenements = mutableListOf<String>()
        val job = launch(testDispatcher) { viewModel.actionEvenement.collect { evenements += it } }

        viewModel.supprimerUtilisateur(userA.uid)
        advanceUntilIdle()

        assertTrue(evenements.any { it.contains("Erreur") })
        job.cancel()
    }

    // ── suspendreUtilisateur ──────────────────────────────────────────────────

    @Test
    fun `suspendreUtilisateur - met a jour le flag suspendu localement`() = runTest {
        repo.utilisateurs = mutableListOf(userA)
        viewModel.chargerTousLesUtilisateurs()
        advanceUntilIdle()

        viewModel.suspendreUtilisateur(userA.uid, true)
        advanceUntilIdle()

        val state = viewModel.utilisateursState.value as AdminViewModel.UtilisateursState.Succes
        assertTrue(state.utilisateurs.first { it.uid == userA.uid }.suspendu)
        assertEquals(listOf(userA.uid to true), repo.suspendreAppels)
    }

    @Test
    fun `suspendreUtilisateur - lever suspension met suspendu a false`() = runTest {
        repo.utilisateurs = mutableListOf(userSuspendu)
        viewModel.chargerTousLesUtilisateurs()
        advanceUntilIdle()

        viewModel.suspendreUtilisateur(userSuspendu.uid, false)
        advanceUntilIdle()

        val state = viewModel.utilisateursState.value as AdminViewModel.UtilisateursState.Succes
        assertFalse(state.utilisateurs.first { it.uid == userSuspendu.uid }.suspendu)
    }

    // ── chargerPaliers ────────────────────────────────────────────────────────

    @Test
    fun `chargerPaliers - succes remplit la liste`() = runTest {
        repo.paliers = mutableListOf(palier1, palier2)
        viewModel.chargerPaliers()
        advanceUntilIdle()
        val state = viewModel.paliersState.value as AdminViewModel.PaliersState.Succes
        assertEquals(2, state.paliers.size)
    }

    @Test
    fun `chargerPaliers - liste vide retourne les paliers par defaut`() = runTest {
        repo.paliers = mutableListOf()
        viewModel.chargerPaliers()
        advanceUntilIdle()
        val state = viewModel.paliersState.value as AdminViewModel.PaliersState.Succes
        assertEquals(viewModel.paliersDefaut, state.paliers)
    }

    @Test
    fun `chargerPaliers - erreur produit etat Erreur`() = runTest {
        repo.lirePaliersResult = Result.failure(Exception("firestore down"))
        viewModel.chargerPaliers()
        advanceUntilIdle()
        assertTrue(viewModel.paliersState.value is AdminViewModel.PaliersState.Erreur)
    }

    // ── mettreAJourPalier ─────────────────────────────────────────────────────

    @Test
    fun `mettreAJourPalier - met a jour le palier dans la liste locale`() = runTest {
        repo.paliers = mutableListOf(palier1, palier2)
        viewModel.chargerPaliers()
        advanceUntilIdle()

        val palierMaj = palier1.copy(label = "Novice", seuilMin = 0, seuilMax = 199)
        viewModel.mettreAJourPalier(palierMaj)
        advanceUntilIdle()

        val state = viewModel.paliersState.value as AdminViewModel.PaliersState.Succes
        assertEquals("Novice", state.paliers.first { it.id == "p1" }.label)
        assertEquals(listOf(palierMaj), repo.mettreAJourPalierAppels)
    }

    @Test
    fun `mettreAJourPalier - echec emet evenement d erreur`() = runTest {
        repo.paliers = mutableListOf(palier1)
        viewModel.chargerPaliers()
        advanceUntilIdle()
        repo.mettreAJourPalierResult = Result.failure(Exception("write failed"))

        val evenements = mutableListOf<String>()
        val job = launch(testDispatcher) { viewModel.actionEvenement.collect { evenements += it } }

        viewModel.mettreAJourPalier(palier1.copy(label = "Test"))
        advanceUntilIdle()

        assertTrue(evenements.any { it.contains("Erreur") })
        job.cancel()
    }

    // ── chargerStats ──────────────────────────────────────────────────────────

    @Test
    fun `chargerStats - succes remplit les stats`() = runTest {
        repo.stats = StatsGlobales(totalUtilisateurs = 42, totalSeances = 100, scoreMoyen = 350.0)
        viewModel.chargerStats()
        advanceUntilIdle()
        val state = viewModel.statsState.value as AdminViewModel.StatsState.Succes
        assertEquals(42, state.stats.totalUtilisateurs)
        assertEquals(100, state.stats.totalSeances)
        assertEquals(350.0, state.stats.scoreMoyen, 0.01)
    }

    @Test
    fun `chargerStats - erreur produit etat Erreur`() = runTest {
        repo.lireStatsResult = Result.failure(Exception("network"))
        viewModel.chargerStats()
        advanceUntilIdle()
        assertTrue(viewModel.statsState.value is AdminViewModel.StatsState.Erreur)
    }

    // ── actionEvenement ───────────────────────────────────────────────────────

    @Test
    fun `supprimerUtilisateur - succes emet evenement de confirmation`() = runTest {
        repo.utilisateurs = mutableListOf(userA)
        viewModel.chargerTousLesUtilisateurs()
        advanceUntilIdle()

        val evenements = mutableListOf<String>()
        val job = launch(testDispatcher) { viewModel.actionEvenement.collect { evenements += it } }

        viewModel.supprimerUtilisateur(userA.uid)
        advanceUntilIdle()

        assertTrue(evenements.any { it.contains("supprimé") })
        job.cancel()
    }

    @Test
    fun `suspendreUtilisateur - succes emet evenement suspendu`() = runTest {
        repo.utilisateurs = mutableListOf(userA)
        viewModel.chargerTousLesUtilisateurs()
        advanceUntilIdle()

        val evenements = mutableListOf<String>()
        val job = launch(testDispatcher) { viewModel.actionEvenement.collect { evenements += it } }

        viewModel.suspendreUtilisateur(userA.uid, true)
        advanceUntilIdle()

        assertTrue(evenements.any { it.contains("suspendu") || it.contains("Suspendu") })
        job.cancel()
    }
}
