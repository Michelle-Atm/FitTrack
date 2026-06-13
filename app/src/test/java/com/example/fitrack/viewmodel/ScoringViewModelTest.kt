package com.example.fitrack.viewmodel

import com.example.fitrack.model.User
import com.example.fitrack.viewmodel.ObjectifViewModel.ProgressionJournaliere
import com.example.fitrack.viewmodel.ScoringViewModel.SeanceDetail
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ScoringViewModelTest {

    private lateinit var vm: ScoringViewModel

    @Before
    fun setUp() {
        vm = ScoringViewModel()
    }

    // ── Score sans activité ───────────────────────────────────────────────────

    @Test
    fun score_zero_progressions_retourne_zero() {
        val result = vm.calculerScoreInterne(emptyList(), User())
        assertEquals(0.0, result.total, 0.001)
    }

    @Test
    fun score_sans_seances_ni_pas_retourne_zero_ou_negatif() {
        val progressions = List(7) { ProgressionJournaliere() }
        val result = vm.calculerScoreInterne(progressions, User())
        assertTrue("Score inactif doit être <= 0", result.total <= 0.0)
    }

    // ── Composantes du score ──────────────────────────────────────────────────

    @Test
    fun score_activite_calcule_avec_met() {
        val seances = listOf(SeanceDetail(dureeMinutes = 30, typeSeance = "course"))
        val result = vm.calculerScoreInterne(emptyList(), User(), seances)
        val attendu = 30 * 8.0 * 0.1  // MET course = 8.0
        assertEquals(attendu, result.scoreActivite, 0.001)
    }

    @Test
    fun score_activite_musculation_met_inferieur_cardio() {
        val cardio = listOf(SeanceDetail(30, "cardio"))
        val muscu  = listOf(SeanceDetail(30, "musculation"))
        val scoreCardio = vm.calculerScoreInterne(emptyList(), User(), cardio).scoreActivite
        val scoreMuscu  = vm.calculerScoreInterne(emptyList(), User(), muscu).scoreActivite
        assertTrue("Cardio MET > Musculation MET", scoreCardio > scoreMuscu)
    }

    @Test
    fun score_nutrition_proportionnel_a_compliance() {
        val progComplet    = ProgressionJournaliere(progressionCalories = 1.0f)
        val progDemiComplet = ProgressionJournaliere(progressionCalories = 0.5f)
        val full = vm.calculerScoreInterne(listOf(progComplet), User()).scoreNutrition
        val half = vm.calculerScoreInterne(listOf(progDemiComplet), User()).scoreNutrition
        assertEquals(50.0, full, 0.001)
        assertEquals(25.0, half, 0.001)
    }

    @Test
    fun score_nutrition_plafonne_a_100_pct() {
        val progSurConsomme = ProgressionJournaliere(progressionCalories = 2.0f)
        val result = vm.calculerScoreInterne(listOf(progSurConsomme), User()).scoreNutrition
        assertEquals(50.0, result, 0.001)  // coercé à 1.0 × 50
    }

    @Test
    fun bonus_streak_proportionnel_au_streak() {
        val user7j  = User(streakJours = 7)
        val user14j = User(streakJours = 14)
        val score7  = vm.calculerScoreInterne(emptyList(), user7j).bonusStreak
        val score14 = vm.calculerScoreInterne(emptyList(), user14j).bonusStreak
        assertEquals(35.0, score7, 0.001)
        assertEquals(70.0, score14, 0.001)
    }

    @Test
    fun malus_inactivite_applique_sur_jours_sans_activite() {
        val progressions = List(3) {
            ProgressionJournaliere(progressionSeances = 0f, progressionPas = 0.1f)
        }
        val result = vm.calculerScoreInterne(progressions, User())
        assertEquals(-30.0, result.malusInactivite, 0.001)
    }

    @Test
    fun bonus_expert_multiplie_score_par_1_05() {
        val userDebutant = User(experience = "debutant", streakJours = 10)
        val userAvance   = User(experience = "avance",   streakJours = 10)
        val scoreDebutant = vm.calculerScoreInterne(emptyList(), userDebutant)
        val scoreAvance   = vm.calculerScoreInterne(emptyList(), userAvance)
        assertEquals(scoreDebutant.total * 1.05, scoreAvance.total, 0.1)
    }

    // ── Total ne peut pas être négatif ────────────────────────────────────────

    @Test
    fun score_total_jamais_negatif() {
        val pireScenario = List(7) {
            ProgressionJournaliere(progressionSeances = 0f, progressionPas = 0f, progressionCalories = 0f)
        }
        val result = vm.calculerScoreInterne(pireScenario, User())
        assertTrue("Score total doit être >= 0", result.total >= 0.0)
    }

    // ── MET facteurs ─────────────────────────────────────────────────────────

    @Test
    fun met_facteur_course_est_8() {
        assertEquals(8.0, ScoringViewModel.metFacteur("course"), 0.001)
    }

    @Test
    fun met_facteur_marche_est_3_5() {
        assertEquals(3.5, ScoringViewModel.metFacteur("marche"), 0.001)
    }

    @Test
    fun met_facteur_inconnu_retourne_valeur_par_defaut() {
        val met = ScoringViewModel.metFacteur("sport_inconnu")
        assertTrue("MET par défaut doit être > 0", met > 0.0)
    }

    // ── Listes bonus/malus ────────────────────────────────────────────────────

    @Test
    fun bonus_streak_7_jours_genere_message_bonus() {
        val user = User(streakJours = 7)
        val result = vm.calculerScoreInterne(emptyList(), user)
        assertTrue("Doit contenir message streak", result.bonusAppliques.any { it.contains("Streak") })
    }

    @Test
    fun malus_3_jours_inactifs_genere_message_malus() {
        val progressions = List(4) {
            ProgressionJournaliere(progressionSeances = 0f, progressionPas = 0.1f)
        }
        val result = vm.calculerScoreInterne(progressions, User())
        assertTrue("Doit contenir message malus", result.malusAppliques.isNotEmpty())
    }
}
