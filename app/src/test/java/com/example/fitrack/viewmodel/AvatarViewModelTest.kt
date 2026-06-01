package com.example.fitrack.viewmodel

import com.example.fitrack.fakes.FakeAuthRepository
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
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AvatarViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeAuthRepository
    private lateinit var vm: AvatarViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeAuthRepository()
        vm = AvatarViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── determinerEtatActuel ────────────────────────────────────────────────

    @Test
    fun `determinerEtatActuel - xp 0 retourne triste`() {
        assertEquals("triste", vm.determinerEtatActuel(0))
    }

    @Test
    fun `determinerEtatActuel - limite haute triste (74)`() {
        assertEquals("triste", vm.determinerEtatActuel(74))
    }

    @Test
    fun `determinerEtatActuel - limite basse neutre (75)`() {
        assertEquals("neutre", vm.determinerEtatActuel(75))
    }

    @Test
    fun `determinerEtatActuel - limite haute neutre (149)`() {
        assertEquals("neutre", vm.determinerEtatActuel(149))
    }

    @Test
    fun `determinerEtatActuel - limite basse heureux (150)`() {
        assertEquals("heureux", vm.determinerEtatActuel(150))
    }

    @Test
    fun `determinerEtatActuel - limite haute heureux (224)`() {
        assertEquals("heureux", vm.determinerEtatActuel(224))
    }

    @Test
    fun `determinerEtatActuel - limite basse champion (225)`() {
        assertEquals("champion", vm.determinerEtatActuel(225))
    }

    @Test
    fun `determinerEtatActuel - xp max dans niveau (299) retourne champion`() {
        assertEquals("champion", vm.determinerEtatActuel(299))
    }

    // ─── calculerNiveau ──────────────────────────────────────────────────────

    @Test
    fun `calculerNiveau - xp 0 retourne niveau 1`() {
        assertEquals(1, vm.calculerNiveau(0))
    }

    @Test
    fun `calculerNiveau - xp 299 reste niveau 1`() {
        assertEquals(1, vm.calculerNiveau(299))
    }

    @Test
    fun `calculerNiveau - xp 300 passe niveau 2`() {
        assertEquals(2, vm.calculerNiveau(300))
    }

    @Test
    fun `calculerNiveau - xp 599 reste niveau 2`() {
        assertEquals(2, vm.calculerNiveau(599))
    }

    @Test
    fun `calculerNiveau - xp 600 passe niveau 3`() {
        assertEquals(3, vm.calculerNiveau(600))
    }

    @Test
    fun `calculerNiveau - xp 3000 retourne niveau 11`() {
        assertEquals(11, vm.calculerNiveau(3000))
    }

    // ─── chargerAvatar ───────────────────────────────────────────────────────

    @Test
    fun `etat initial - avatar null, isChargement false`() {
        assertNull(vm.avatar.value)
        assertFalse(vm.isChargement.value)
    }

    @Test
    fun `chargerAvatar succes - avatar renseigne avec bons champs`() = runTest {
        repo.recupererResult = Result.success(User(uid = "u1", niveau = 3, xp = 750))
        vm.chargerAvatar("u1")
        advanceUntilIdle()
        val avatar = vm.avatar.value
        assertNotNull(avatar)
        assertEquals("u1", avatar!!.userId)
        assertEquals(3, avatar.niveau)
        assertEquals(750, avatar.xpCumule)
    }

    @Test
    fun `chargerAvatar succes - xpProgression correcte (xp dans niveau)`() = runTest {
        // xp=750, xpDansNiveau = 750 % 300 = 150 → progression = 150/300 = 0.5
        repo.recupererResult = Result.success(User(uid = "u1", niveau = 3, xp = 750))
        vm.chargerAvatar("u1")
        advanceUntilIdle()
        assertEquals(0.5f, vm.xpProgression.value, 0.001f)
    }

    @Test
    fun `chargerAvatar succes - prochainNiveau est niveau+1`() = runTest {
        repo.recupererResult = Result.success(User(uid = "u1", niveau = 4, xp = 1200))
        vm.chargerAvatar("u1")
        advanceUntilIdle()
        assertEquals(5, vm.prochainNiveau.value)
    }

    @Test
    fun `chargerAvatar succes - xpPourNiveau toujours 300`() = runTest {
        repo.recupererResult = Result.success(User(uid = "u1", niveau = 7, xp = 2100))
        vm.chargerAvatar("u1")
        advanceUntilIdle()
        assertEquals(300, vm.xpPourNiveau.value)
    }

    @Test
    fun `chargerAvatar echec - isChargement revient a false`() = runTest {
        repo.recupererResult = Result.failure(Exception("réseau"))
        vm.chargerAvatar("u1")
        advanceUntilIdle()
        assertFalse(vm.isChargement.value)
    }

    @Test
    fun `chargerAvatar echec - avatar reste null`() = runTest {
        repo.recupererResult = Result.failure(Exception("réseau"))
        vm.chargerAvatar("u1")
        advanceUntilIdle()
        assertNull(vm.avatar.value)
    }

    // ─── ajouterXp ───────────────────────────────────────────────────────────

    @Test
    fun `ajouterXp sans montee de niveau - historique reste vide`() = runTest {
        repo.recupererResult = Result.success(User(uid = "u1", niveau = 1, xp = 100))
        repo.mettreAJourResult = Result.success(Unit)
        vm.ajouterXp("u1", 50)
        advanceUntilIdle()
        assertTrue(vm.historiqueEvolution.value.isEmpty())
    }

    @Test
    fun `ajouterXp avec montee de niveau - historique contient entree`() = runTest {
        repo.recupererResult = Result.success(User(uid = "u1", niveau = 1, xp = 250))
        repo.mettreAJourResult = Result.success(Unit)
        vm.ajouterXp("u1", 100) // 250+100=350 → niveau 2
        advanceUntilIdle()
        assertEquals(1, vm.historiqueEvolution.value.size)
        assertTrue(vm.historiqueEvolution.value[0].contains("Niveau 2"))
        assertTrue(vm.historiqueEvolution.value[0].contains("+100 XP"))
    }

    @Test
    fun `ajouterXp avec montee de niveau - avatar mis a jour`() = runTest {
        repo.recupererResult = Result.success(User(uid = "u1", niveau = 1, xp = 250))
        repo.mettreAJourResult = Result.success(Unit)
        vm.ajouterXp("u1", 100)
        advanceUntilIdle()
        assertEquals(2, vm.avatar.value?.niveau)
    }

    @Test
    fun `ajouterXp mettreAJourProfil echec - UI non modifiee`() = runTest {
        repo.recupererResult = Result.success(User(uid = "u1", niveau = 1, xp = 100))
        repo.mettreAJourResult = Result.success(Unit)
        vm.chargerAvatar("u1")
        advanceUntilIdle()
        val niveauAvant = vm.avatar.value?.niveau

        repo.mettreAJourResult = Result.failure(Exception("offline"))
        vm.ajouterXp("u1", 50)
        advanceUntilIdle()
        assertEquals(niveauAvant, vm.avatar.value?.niveau)
    }

    @Test
    fun `ajouterXp plusieurs appels - historique accumule dans l ordre`() = runTest {
        repo.recupererResult = Result.success(User(uid = "u1", niveau = 1, xp = 250))
        repo.mettreAJourResult = Result.success(Unit)
        vm.ajouterXp("u1", 100) // → niveau 2
        advanceUntilIdle()

        repo.recupererResult = Result.success(User(uid = "u1", niveau = 2, xp = 550))
        vm.ajouterXp("u1", 100) // → niveau 3
        advanceUntilIdle()

        assertEquals(2, vm.historiqueEvolution.value.size)
        assertTrue(vm.historiqueEvolution.value[0].contains("Niveau 3"))
        assertTrue(vm.historiqueEvolution.value[1].contains("Niveau 2"))
    }
}
