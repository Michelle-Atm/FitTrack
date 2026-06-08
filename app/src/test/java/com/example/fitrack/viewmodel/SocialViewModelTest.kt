package com.example.fitrack.viewmodel

import com.example.fitrack.fakes.FakeSocialRepository
import com.example.fitrack.model.ProfilPublic
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
class SocialViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()
    private lateinit var repo: FakeSocialRepository
    private lateinit var viewModel: SocialViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        repo = FakeSocialRepository()
        viewModel = SocialViewModel(repo)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private fun profils(vararg triplets: Triple<String, Double, String>): List<ProfilPublic> =
        triplets.map { (id, score, cat) ->
            ProfilPublic(userId = id, nom = "User $id", scoreHebdo = score, categorie = cat)
        }

    // --- Classement global ---

    @Test
    fun `chargerClassement trie par score decroissant`() = runTest {
        repo.lireClassementResult = Result.success(
            profils(
                Triple("u1", 500.0, "debutant"),
                Triple("u2", 900.0, "intermediaire"),
                Triple("u3", 300.0, "avance")
            )
        )
        viewModel.chargerClassement("u1", "debutant")
        advanceUntilIdle()

        val global = viewModel.classementGlobal.value
        assertEquals("u2", global[0].userId)
        assertEquals("u1", global[1].userId)
        assertEquals("u3", global[2].userId)
    }

    @Test
    fun `chargerClassement attribue les rangs correctement`() = runTest {
        repo.lireClassementResult = Result.success(
            profils(
                Triple("u1", 800.0, "debutant"),
                Triple("u2", 600.0, "debutant"),
                Triple("u3", 400.0, "debutant")
            )
        )
        viewModel.chargerClassement("u1", "debutant")
        advanceUntilIdle()

        val global = viewModel.classementGlobal.value
        assertEquals(1, global[0].rang)
        assertEquals(2, global[1].rang)
        assertEquals(3, global[2].rang)
    }

    @Test
    fun `chargerClassement met a jour monRang`() = runTest {
        repo.lireClassementResult = Result.success(
            profils(
                Triple("u1", 300.0, "debutant"),
                Triple("u2", 800.0, "debutant"),
                Triple("moi", 500.0, "debutant")
            )
        )
        viewModel.chargerClassement("moi", "debutant")
        advanceUntilIdle()

        assertEquals(2, viewModel.monRang.value)
    }

    @Test
    fun `chargerClassement met a jour monScoreHebdo`() = runTest {
        repo.lireClassementResult = Result.success(
            profils(Triple("moi", 742.0, "debutant"))
        )
        viewModel.chargerClassement("moi", "debutant")
        advanceUntilIdle()

        assertEquals(742.0, viewModel.monScoreHebdo.value, 0.01)
    }

    @Test
    fun `chargerClassement monRang vaut 0 si userId absent`() = runTest {
        repo.lireClassementResult = Result.success(
            profils(Triple("u1", 500.0, "debutant"))
        )
        viewModel.chargerClassement("inconnu", "debutant")
        advanceUntilIdle()

        assertEquals(0, viewModel.monRang.value)
    }

    // --- Classement catégorie ---

    @Test
    fun `classementCategorie filtre sur la categorie demandee`() = runTest {
        repo.lireClassementResult = Result.success(
            profils(
                Triple("u1", 900.0, "avance"),
                Triple("u2", 700.0, "debutant"),
                Triple("u3", 500.0, "debutant"),
                Triple("u4", 300.0, "intermediaire")
            )
        )
        viewModel.chargerClassement("u2", "debutant")
        advanceUntilIdle()

        val cat = viewModel.classementCategorie.value
        assertEquals(2, cat.size)
        assertTrue(cat.all { it.categorie == "debutant" })
    }

    @Test
    fun `classementCategorie rangs recommencent a 1`() = runTest {
        repo.lireClassementResult = Result.success(
            profils(
                Triple("u1", 900.0, "avance"),
                Triple("u2", 700.0, "debutant"),
                Triple("u3", 500.0, "debutant")
            )
        )
        viewModel.chargerClassement("u2", "debutant")
        advanceUntilIdle()

        val cat = viewModel.classementCategorie.value
        assertEquals(1, cat[0].rang)
        assertEquals(2, cat[1].rang)
    }

    @Test
    fun `monRangCategorie correct dans sa categorie`() = runTest {
        repo.lireClassementResult = Result.success(
            profils(
                Triple("u1", 900.0, "avance"),
                Triple("u2", 700.0, "debutant"),
                Triple("moi", 400.0, "debutant")
            )
        )
        viewModel.chargerClassement("moi", "debutant")
        advanceUntilIdle()

        assertEquals(2, viewModel.monRangCategorie.value)
    }

    @Test
    fun `classementCategorie vide si aucun membre dans la categorie`() = runTest {
        repo.lireClassementResult = Result.success(
            profils(Triple("u1", 900.0, "avance"))
        )
        viewModel.chargerClassement("u1", "debutant")
        advanceUntilIdle()

        assertTrue(viewModel.classementCategorie.value.isEmpty())
    }

    // --- Liste vide ---

    @Test
    fun `chargerClassement liste vide ne crashe pas`() = runTest {
        repo.lireClassementResult = Result.success(emptyList())
        viewModel.chargerClassement("moi", "debutant")
        advanceUntilIdle()

        assertTrue(viewModel.classementGlobal.value.isEmpty())
        assertEquals(0, viewModel.monRang.value)
    }

    // --- publierMonProfil ---

    @Test
    fun `publierMonProfil appelle mettreAJourProfil avec les bonnes valeurs`() = runTest {
        viewModel.publierMonProfil(
            userId = "u1",
            nom = "Alice",
            avatarEspece = "renard",
            avatarEtat = "champion",
            categorie = "avance",
            scoreHebdo = 1234.0,
            xpTotal = 3600,
            niveauActuel = 12
        )
        advanceUntilIdle()

        assertEquals(1, repo.profilsMisAJour.size)
        val profil = repo.profilsMisAJour[0]
        assertEquals("u1", profil.userId)
        assertEquals("Alice", profil.nom)
        assertEquals(1234.0, profil.scoreHebdo, 0.01)
        assertEquals(12, profil.niveauActuel)
    }

    // --- chargerProfilPublic ---

    @Test
    fun `chargerProfilPublic expose le profil trouve`() = runTest {
        repo.lireClassementResult = Result.success(
            listOf(ProfilPublic(userId = "u42", nom = "Bob", scoreHebdo = 999.0))
        )
        viewModel.chargerProfilPublic("u42")
        advanceUntilIdle()

        val profil = viewModel.profilConsulte.value
        assertEquals("u42", profil?.userId)
        assertEquals("Bob", profil?.nom)
    }

    @Test
    fun `chargerProfilPublic reste null si userId absent`() = runTest {
        repo.lireClassementResult = Result.success(emptyList())
        viewModel.chargerProfilPublic("inexistant")
        advanceUntilIdle()

        assertEquals(null, viewModel.profilConsulte.value)
    }
}
