package com.example.fitrack.viewmodel

import com.example.fitrack.viewmodel.GyroscopeViewModel.TypeMouvement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.sqrt

class GyroscopeViewModelTest {

    // ── detecterMouvement (logique pure, pas d'AndroidViewModel) ────────────

    @Test
    fun statique_si_intensite_zero() {
        assertEquals(TypeMouvement.STATIQUE, GyroscopeViewModel.detecterMouvement(0.0f))
    }

    @Test
    fun statique_si_intensite_juste_en_dessous_du_seuil() {
        assertEquals(TypeMouvement.STATIQUE, GyroscopeViewModel.detecterMouvement(0.14f))
    }

    @Test
    fun marche_au_seuil_bas() {
        assertEquals(TypeMouvement.MARCHE, GyroscopeViewModel.detecterMouvement(0.15f))
    }

    @Test
    fun marche_juste_sous_le_seuil_course() {
        assertEquals(TypeMouvement.MARCHE, GyroscopeViewModel.detecterMouvement(0.99f))
    }

    @Test
    fun course_au_seuil() {
        assertEquals(TypeMouvement.COURSE, GyroscopeViewModel.detecterMouvement(1.0f))
    }

    @Test
    fun course_juste_sous_le_seuil_velo() {
        assertEquals(TypeMouvement.COURSE, GyroscopeViewModel.detecterMouvement(2.49f))
    }

    @Test
    fun velo_au_seuil() {
        assertEquals(TypeMouvement.VELO, GyroscopeViewModel.detecterMouvement(2.5f))
    }

    @Test
    fun velo_valeur_haute() {
        assertEquals(TypeMouvement.VELO, GyroscopeViewModel.detecterMouvement(10.0f))
    }

    @Test
    fun tous_4_types_atteignables() {
        val types = setOf(
            GyroscopeViewModel.detecterMouvement(0.0f),
            GyroscopeViewModel.detecterMouvement(0.5f),
            GyroscopeViewModel.detecterMouvement(1.5f),
            GyroscopeViewModel.detecterMouvement(3.0f)
        )
        assertEquals(4, types.size)
    }

    @Test
    fun valeur_max_float_ne_crashe_pas() {
        // Ne doit pas lever d'exception — le résultat concret importe peu
        val result = GyroscopeViewModel.detecterMouvement(Float.MAX_VALUE)
        assertEquals(TypeMouvement.VELO, result)
    }

    // ── DonneesGyroscope.intensite ────────────────────────────────────────────

    @Test
    fun intensite_zero_si_toutes_axes_nulles() {
        val d = GyroscopeViewModel.DonneesGyroscope(0f, 0f, 0f)
        assertEquals(0.0f, d.intensite, 0.001f)
    }

    @Test
    fun intensite_pythagoricienne_3_4_0_egale_5() {
        val d = GyroscopeViewModel.DonneesGyroscope(x = 3f, y = 4f, z = 0f)
        assertEquals(5.0f, d.intensite, 0.001f)
    }

    @Test
    fun intensite_axe_unique_z() {
        val d = GyroscopeViewModel.DonneesGyroscope(0f, 0f, 2.5f)
        assertEquals(2.5f, d.intensite, 0.001f)
    }

    @Test
    fun intensite_toujours_positive() {
        val d = GyroscopeViewModel.DonneesGyroscope(-3f, -4f, 0f)
        assertTrue("Intensité doit être positive", d.intensite >= 0f)
        assertEquals(5.0f, d.intensite, 0.001f)
    }

    // ── Cohérence seuils / types ─────────────────────────────────────────────

    @Test
    fun seuils_continus_sans_trou() {
        val attendu = mapOf(
            0.0f  to TypeMouvement.STATIQUE,
            0.14f to TypeMouvement.STATIQUE,
            0.15f to TypeMouvement.MARCHE,
            0.99f to TypeMouvement.MARCHE,
            1.0f  to TypeMouvement.COURSE,
            2.49f to TypeMouvement.COURSE,
            2.5f  to TypeMouvement.VELO,
            5.0f  to TypeMouvement.VELO
        )
        attendu.forEach { (intensite, type) ->
            assertEquals("Mauvais type pour intensite=$intensite", type,
                GyroscopeViewModel.detecterMouvement(intensite))
        }
    }

    @Test
    fun intensite_negative_ne_crashe_pas() {
        // Physiquement impossible (sqrt toujours >= 0), mais test défensif
        // -1.0 < 0.15 → STATIQUE
        val result = GyroscopeViewModel.detecterMouvement(-1.0f)
        assertEquals(TypeMouvement.STATIQUE, result)
    }
}
