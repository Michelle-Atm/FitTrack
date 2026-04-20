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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeAuthRepository
    private lateinit var viewModel: AuthViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeAuthRepository()
        viewModel = AuthViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // --- calculerIMC ---

    @Test
    fun `calculerIMC retourne valeur correcte`() {
        val imc = viewModel.calculerIMC(70.0, 175)
        assertEquals(22.9, imc, 0.01)
    }

    @Test
    fun `calculerIMC retourne 0 si poids nul`() {
        assertEquals(0.0, viewModel.calculerIMC(0.0, 175), 0.0)
    }

    @Test
    fun `calculerIMC retourne 0 si taille nulle`() {
        assertEquals(0.0, viewModel.calculerIMC(70.0, 0), 0.0)
    }

    @Test
    fun `calculerIMC arrondi a une decimale`() {
        val imc = viewModel.calculerIMC(85.0, 180)
        assertEquals(26.2, imc, 0.01)
    }

    // --- categorieIMC ---

    @Test
    fun `categorieIMC sous-poids`() {
        assertEquals("Sous-poids", viewModel.categorieIMC(17.0))
    }

    @Test
    fun `categorieIMC poids normal`() {
        assertEquals("Poids normal", viewModel.categorieIMC(22.0))
    }

    @Test
    fun `categorieIMC surpoids`() {
        assertEquals("Surpoids", viewModel.categorieIMC(27.5))
    }

    @Test
    fun `categorieIMC obesite moderee`() {
        assertEquals("Obésité modérée", viewModel.categorieIMC(32.0))
    }

    @Test
    fun `categorieIMC obesite severe`() {
        assertEquals("Obésité sévère", viewModel.categorieIMC(40.0))
    }

    @Test
    fun `categorieIMC non calcule si zero`() {
        assertEquals("Non calculé", viewModel.categorieIMC(0.0))
    }

    @Test
    fun `categorieIMC limite basse poids normal`() {
        assertEquals("Poids normal", viewModel.categorieIMC(18.5))
    }

    @Test
    fun `categorieIMC limite haute poids normal`() {
        assertEquals("Surpoids", viewModel.categorieIMC(25.0))
    }

    // --- genererProgramme ---

    @Test
    fun `genererProgramme perte poids debutant contient cardio`() {
        val user = User(objectif = "perte_poids", experience = "debutant", disponibilites = listOf("lundi", "jeudi"))
        val programme = viewModel.genererProgramme(user)
        assertTrue(programme.contains("Cardio"))
        assertTrue(programme.contains("2 séance(s)/sem"))
    }

    @Test
    fun `genererProgramme prise masse intermediaire contient PPL`() {
        val user = User(objectif = "prise_masse", experience = "intermediaire", disponibilites = listOf("lundi", "mercredi", "vendredi"))
        val programme = viewModel.genererProgramme(user)
        assertTrue(programme.contains("PPL"))
    }

    @Test
    fun `genererProgramme endurance contient zone 2`() {
        val user = User(objectif = "endurance", experience = "avance", disponibilites = listOf("lundi"))
        val programme = viewModel.genererProgramme(user)
        assertTrue(programme.contains("zone 2"))
    }

    @Test
    fun `genererProgramme mentionne allergies`() {
        val user = User(objectif = "perte_poids", experience = "debutant", allergies = listOf("gluten", "lactose"))
        val programme = viewModel.genererProgramme(user)
        assertTrue(programme.contains("gluten"))
        assertTrue(programme.contains("lactose"))
    }

    @Test
    fun `genererProgramme defaut si objectif inconnu`() {
        val user = User(objectif = "inconnu", experience = "debutant")
        val programme = viewModel.genererProgramme(user)
        assertTrue(programme.contains("Équilibre"))
    }

    @Test
    fun `genererProgramme au moins 1 seance si disponibilites vides`() {
        val user = User(objectif = "endurance", disponibilites = emptyList())
        val programme = viewModel.genererProgramme(user)
        assertTrue(programme.contains("1 séance(s)/sem"))
    }

    // --- connexion (validation + state) ---

    @Test
    fun `connexion email vide emet erreur sans appel repo`() {
        viewModel.connexion("", "password123")
        val state = viewModel.uiState.value
        assertTrue(state is AuthViewModel.AuthUiState.Erreur)
        assertEquals("Email et mot de passe requis", (state as AuthViewModel.AuthUiState.Erreur).message)
    }

    @Test
    fun `connexion mot de passe vide emet erreur`() {
        viewModel.connexion("test@test.com", "")
        assertTrue(viewModel.uiState.value is AuthViewModel.AuthUiState.Erreur)
    }

    @Test
    fun `connexion succes met a jour etat`() = runTest {
        viewModel.connexion("test@test.com", "password123")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is AuthViewModel.AuthUiState.Succes)
    }

    @Test
    fun `connexion echec emet erreur`() = runTest {
        repo.connexionResult = Result.failure(Exception("Identifiants invalides"))
        viewModel.connexion("test@test.com", "mauvais")
        advanceUntilIdle()
        val state = viewModel.uiState.value
        assertTrue(state is AuthViewModel.AuthUiState.Erreur)
        assertEquals("Identifiants invalides", (state as AuthViewModel.AuthUiState.Erreur).message)
    }

    // --- inscrire (validation) ---

    @Test
    fun `inscrire nom vide emet erreur`() {
        val user = User(nom = "")
        viewModel.inscrire("test@test.com", "password123", user)
        assertTrue(viewModel.uiState.value is AuthViewModel.AuthUiState.Erreur)
    }

    @Test
    fun `inscrire email vide emet erreur`() {
        val user = User(nom = "Alice")
        viewModel.inscrire("", "password123", user)
        assertTrue(viewModel.uiState.value is AuthViewModel.AuthUiState.Erreur)
    }

    @Test
    fun `inscrire succes calcule imc et programme`() = runTest {
        val user = User(nom = "Alice", poids = 60.0, taille = 165, objectif = "perte_poids", experience = "debutant")
        repo.inscriptionResult = Result.success(user.copy(uid = "uid1", imc = 22.0))
        viewModel.inscrire("alice@test.com", "password123", user)
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is AuthViewModel.AuthUiState.Succes)
    }

    // --- deconnexion ---

    @Test
    fun `deconnexion met etat a Deconnecte`() = runTest {
        viewModel.deconnexion()
        advanceUntilIdle()
        assertTrue(viewModel.uiState.value is AuthViewModel.AuthUiState.Deconnecte)
    }

    // --- reinitialiserEtat ---

    @Test
    fun `reinitialiserEtat remet etat a Initial`() = runTest {
        viewModel.connexion("test@test.com", "password123")
        advanceUntilIdle()
        viewModel.reinitialiserEtat()
        assertTrue(viewModel.uiState.value is AuthViewModel.AuthUiState.Initial)
    }
}
