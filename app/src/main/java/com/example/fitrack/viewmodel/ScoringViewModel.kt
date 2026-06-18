package com.example.fitrack.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitrack.model.User
import com.example.fitrack.viewmodel.ObjectifViewModel.ProgressionJournaliere
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Formule scoring hebdomadaire FitTrack :
 *
 *   scoreActivite    = Σ(duréeMin × MET_facteur × 0.1)   [par séance de la semaine]
 *   scoreNutrition   = Σ(compliance_calorique_jour × 50)  [0..50 pts/jour]
 *   scoreConsistance = Σ(progressionPas_jour × 30)        [0..30 pts/jour]
 *   bonusStreak      = streakJours × 5                    [pts/jour de streak]
 *   bonusObjectif    = nombre_jours_objectif_atteint × 20
 *   malusInactivite  = nombre_jours_sans_activite × (-10)
 *
 *   TOTAL = scoreActivite + scoreNutrition + scoreConsistance
 *         + bonusStreak + bonusObjectif + malusInactivite
 *
 * Max théorique (7 jours parfaits, 30 min cardio/jour, streak 30j) ≈ 1 400 pts
 */
class ScoringViewModel : ViewModel() {

    data class ScoreHebdomadaire(
        val total: Double = 0.0,
        val scoreActivite: Double = 0.0,
        val scoreNutrition: Double = 0.0,
        val scoreConsistance: Double = 0.0,
        val bonusStreak: Double = 0.0,
        val bonusObjectif: Double = 0.0,
        val malusInactivite: Double = 0.0,
        val bonusAppliques: List<String> = emptyList(),
        val malusAppliques: List<String> = emptyList()
    )

    private val _score = MutableStateFlow(ScoreHebdomadaire())
    val score: StateFlow<ScoreHebdomadaire> = _score.asStateFlow()

    fun calculerScore(
        progressions: List<ProgressionJournaliere>,
        user: User,
        seancesDetails: List<SeanceDetail> = emptyList()
    ) {
        viewModelScope.launch {
            _score.value = calculerScoreInterne(progressions, user, seancesDetails)
        }
    }

    fun calculerScoreInterne(
        progressions: List<ProgressionJournaliere>,
        user: User,
        seancesDetails: List<SeanceDetail> = emptyList()
    ): ScoreHebdomadaire {
        val scoreActivite = seancesDetails.sumOf { s ->
            // Cap at 10h per session to prevent fraudulent score inflation
            val dureeValide = s.dureeMinutes.coerceIn(0, 600)
            dureeValide * metFacteur(s.typeSeance) * 0.1
        }

        val scoreNutrition = progressions.sumOf { p ->
            p.progressionCalories.toDouble().coerceIn(0.0, 1.0) * 50.0
        }

        val scoreConsistance = progressions.sumOf { p ->
            p.progressionPas.toDouble().coerceIn(0.0, 1.0) * 30.0
        }

        val bonusStreak = user.streakJours * 5.0

        val joursObjectifAtteint = progressions.count { p ->
            p.progressionCalories >= 0.9f && p.progressionSeances >= 1f
        }
        val bonusObjectif = joursObjectifAtteint * 20.0

        val joursInactifs = progressions.count { p ->
            p.progressionSeances == 0f && p.progressionPas < 0.3f
        }
        val malusInactivite = joursInactifs * (-10.0)

        val bonusAppliques = buildList {
            if (user.streakJours >= 7) add("Streak 7 jours (+${(user.streakJours * 5).toInt()} pts)")
            if (joursObjectifAtteint >= 5) add("5+ jours objectif atteint (+${joursObjectifAtteint * 20} pts)")
            if (user.experience == "avance") add("Bonus expert (+5%)")
        }
        val malusAppliques = buildList {
            if (joursInactifs >= 3) add("$joursInactifs jours inactifs (${joursInactifs * (-10)} pts)")
        }

        val expertMultiplier = if (user.experience == "avance") 1.05 else 1.0

        val total = (scoreActivite + scoreNutrition + scoreConsistance +
                bonusStreak + bonusObjectif + malusInactivite) * expertMultiplier

        return ScoreHebdomadaire(
            total = total.coerceAtLeast(0.0),
            scoreActivite = scoreActivite,
            scoreNutrition = scoreNutrition,
            scoreConsistance = scoreConsistance,
            bonusStreak = bonusStreak,
            bonusObjectif = bonusObjectif,
            malusInactivite = malusInactivite,
            bonusAppliques = bonusAppliques,
            malusAppliques = malusAppliques
        )
    }

    data class SeanceDetail(
        val dureeMinutes: Int,
        val typeSeance: String
    )

    companion object {
        fun metFacteur(typeSeance: String): Double = when (typeSeance) {
            "course"      -> 8.0
            "velo"        -> 7.5
            "natation"    -> 7.0
            "cardio"      -> 7.0
            "musculation" -> 6.0
            "marche"      -> 3.5
            "yoga"        -> 3.0
            else          -> 5.0
        }

        val Factory: ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    ScoringViewModel() as T
            }
    }
}
