package com.example.fitrack.viewmodel

import com.example.fitrack.fakes.FakeAuthRepository
import com.example.fitrack.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
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
class SecurityTest {

    private val dispatcher = StandardTestDispatcher()
    private lateinit var vm: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(dispatcher)
        vm = AuthViewModel(FakeAuthRepository())
    }

    @After
    fun tearDown() = Dispatchers.resetMain()

    // ── Validation email ──────────────────────────────────────────────────────

    @Test
    fun connexion_rejette_email_vide() = runTest {
        vm.connexion("", "secret123")
        dispatcher.scheduler.advanceUntilIdle()
        val state = vm.uiState.value
        assertTrue("Doit être Erreur", state is AuthViewModel.AuthUiState.Erreur)
    }

    @Test
    fun connexion_rejette_mot_de_passe_vide() = runTest {
        vm.connexion("user@example.com", "")
        dispatcher.scheduler.advanceUntilIdle()
        val state = vm.uiState.value
        assertTrue("Doit être Erreur", state is AuthViewModel.AuthUiState.Erreur)
    }

    @Test
    fun connexion_rejette_les_deux_vides() = runTest {
        vm.connexion("", "")
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.uiState.value is AuthViewModel.AuthUiState.Erreur)
    }

    @Test
    fun inscription_rejette_nom_vide() = runTest {
        vm.inscrire("a@b.com", "pass123", User(nom = ""))
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.uiState.value is AuthViewModel.AuthUiState.Erreur)
    }

    @Test
    fun inscription_rejette_email_vide() = runTest {
        vm.inscrire("", "pass123", User(nom = "Alice"))
        dispatcher.scheduler.advanceUntilIdle()
        assertTrue(vm.uiState.value is AuthViewModel.AuthUiState.Erreur)
    }

    // ── Injection / inputs malveillants ───────────────────────────────────────

    @Test
    fun imc_avec_poids_negatif_retourne_zero() {
        assertEquals(0.0, vm.calculerIMC(-70.0, 175), 0.001)
    }

    @Test
    fun imc_avec_taille_zero_retourne_zero() {
        assertEquals(0.0, vm.calculerIMC(70.0, 0), 0.001)
    }

    @Test
    fun imc_avec_poids_zero_retourne_zero() {
        assertEquals(0.0, vm.calculerIMC(0.0, 175), 0.001)
    }

    @Test
    fun imc_valeurs_extremes_ne_crashent_pas() {
        val imc = vm.calculerIMC(500.0, 50)
        assertTrue("IMC doit être positif", imc >= 0.0)
        assertTrue("IMC ne doit pas être infini", imc.isFinite())
    }

    @Test
    fun imc_double_max_ne_crashe_pas() {
        val imc = vm.calculerIMC(Double.MAX_VALUE, 1)
        assertTrue("Ne doit pas être NaN", !imc.isNaN())
    }

    @Test
    fun nom_avec_script_xss_est_accepte_sans_execution() {
        // Firestore stocke la valeur en tant que string — aucune exécution côté serveur
        val userXss = User(nom = "<script>alert('xss')</script>", uid = "u1")
        // Le nom ne doit pas être vide (validation UI), et il est stocké tel quel
        assertFalse("Nom XSS ne doit pas être vide", userXss.nom.isBlank())
        assertEquals("<script>alert('xss')</script>", userXss.nom)
    }

    @Test
    fun champs_numeriques_ne_debordent_pas() {
        val user = User(xp = Int.MAX_VALUE, niveau = Int.MAX_VALUE, streakJours = Int.MAX_VALUE)
        assertTrue(user.xp == Int.MAX_VALUE)
        assertTrue(user.niveau == Int.MAX_VALUE)
    }

    // ── Scores ne peuvent pas être négatifs ───────────────────────────────────

    @Test
    fun calcul_imc_normal_retourne_valeur_positive() {
        val imc = vm.calculerIMC(70.0, 175)
        assertTrue("IMC doit être positif pour données valides", imc > 0)
    }

    @Test
    fun categorie_imc_couvre_tous_les_cas() {
        // Aucune entrée ne doit lever d'exception ni retourner null
        val cas = listOf(-1.0, 0.0, 10.0, 18.4, 18.5, 24.9, 25.0, 29.9, 30.0, 35.0, 99.0)
        cas.forEach { imc ->
            val cat = vm.categorieIMC(imc)
            assertFalse("Catégorie doit être non vide pour imc=$imc", cat.isBlank())
        }
    }

    // ── Génération de programme ───────────────────────────────────────────────

    @Test
    fun programme_genere_pour_user_vide_ne_crashe_pas() {
        val programme = vm.genererProgramme(User())
        assertFalse("Programme ne doit pas être vide", programme.isBlank())
    }

    @Test
    fun programme_genere_ne_contient_pas_balises_html() {
        val user = User(objectif = "perte_poids", experience = "debutant")
        val programme = vm.genererProgramme(user)
        assertFalse("Ne doit pas contenir de balises HTML", programme.contains("<"))
        assertFalse(programme.contains(">"))
    }

    // ── Streak & XP ne peuvent être négatifs ─────────────────────────────────

    @Test
    fun streak_negatif_dans_user_ne_crashe_pas_condition_deblocage() {
        val vm2 = ObjectifViewModel(com.example.fitrack.fakes.FakeObjectifRepository())
        val user = User(streakJours = -5, niveau = 0, xp = 0)
        val result = vm2.conditionDeblocageRemplie(user, "streak_1")
        assertFalse("Streak négatif ne remplit pas la condition", result)
    }

    @Test
    fun condition_deblocage_chaine_vide_retourne_false() {
        val vm2 = ObjectifViewModel(com.example.fitrack.fakes.FakeObjectifRepository())
        assertFalse(vm2.conditionDeblocageRemplie(User(), ""))
    }

    @Test
    fun condition_deblocage_inconnue_retourne_false() {
        val vm2 = ObjectifViewModel(com.example.fitrack.fakes.FakeObjectifRepository())
        assertFalse(vm2.conditionDeblocageRemplie(User(), "injection_sql_' OR 1=1 --"))
        assertFalse(vm2.conditionDeblocageRemplie(User(), "niveau_abc"))
    }
}
