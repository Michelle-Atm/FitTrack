package com.example.fitrack.viewmodel

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests for GpsViewModel pure logic (companion functions).
 * Android-specific behavior (location tracking, FusedLocationProvider) is
 * covered by instrumented tests.
 */
class GpsViewModelTest {

    // ── metFacteur ────────────────────────────────────────────────────────────

    @Test
    fun `metFacteur course retourne 8_0`() {
        assertEquals(8.0, GpsViewModel.metFacteur("course"), 0.001)
    }

    @Test
    fun `metFacteur velo retourne 7_5`() {
        assertEquals(7.5, GpsViewModel.metFacteur("velo"), 0.001)
    }

    @Test
    fun `metFacteur marche retourne 3_5`() {
        assertEquals(3.5, GpsViewModel.metFacteur("marche"), 0.001)
    }

    @Test
    fun `metFacteur musculation retourne 6_0`() {
        assertEquals(6.0, GpsViewModel.metFacteur("musculation"), 0.001)
    }

    @Test
    fun `metFacteur cardio retourne 7_0`() {
        assertEquals(7.0, GpsViewModel.metFacteur("cardio"), 0.001)
    }

    @Test
    fun `metFacteur yoga retourne 3_0`() {
        assertEquals(3.0, GpsViewModel.metFacteur("yoga"), 0.001)
    }

    @Test
    fun `metFacteur natation retourne 7_0`() {
        assertEquals(7.0, GpsViewModel.metFacteur("natation"), 0.001)
    }

    @Test
    fun `metFacteur type inconnu retourne valeur par defaut positive`() {
        val met = GpsViewModel.metFacteur("activite_inconnue")
        assertTrue("MET par défaut doit être > 0", met > 0.0)
    }

    @Test
    fun `metFacteur course superieur a marche`() {
        assertTrue(GpsViewModel.metFacteur("course") > GpsViewModel.metFacteur("marche"))
    }

    // ── calculerCalories ─────────────────────────────────────────────────────

    @Test
    fun `calculerCalories course 30min 70kg retourne valeur correcte`() {
        // 8.0 MET × 70 kg × (1800s / 3600) = 8.0 × 70 × 0.5 = 280
        val cal = GpsViewModel.calculerCalories("course", 70.0, 1800)
        assertEquals(280.0, cal, 0.001)
    }

    @Test
    fun `calculerCalories marche 60min 80kg retourne valeur correcte`() {
        // 3.5 MET × 80 kg × (3600s / 3600) = 3.5 × 80 × 1.0 = 280
        val cal = GpsViewModel.calculerCalories("marche", 80.0, 3600)
        assertEquals(280.0, cal, 0.001)
    }

    @Test
    fun `calculerCalories duree zero retourne zero`() {
        val cal = GpsViewModel.calculerCalories("course", 70.0, 0)
        assertEquals(0.0, cal, 0.001)
    }

    @Test
    fun `calculerCalories jamais negative`() {
        val cal = GpsViewModel.calculerCalories("course", 0.0, -100)
        assertTrue("Calories ne peuvent pas être négatives", cal >= 0.0)
    }

    @Test
    fun `calculerCalories course brule plus que marche meme duree`() {
        val course = GpsViewModel.calculerCalories("course", 70.0, 3600)
        val marche = GpsViewModel.calculerCalories("marche", 70.0, 3600)
        assertTrue("Course doit brûler plus que marche", course > marche)
    }

    @Test
    fun `calculerCalories proportionnel au poids`() {
        val cal60kg = GpsViewModel.calculerCalories("cardio", 60.0, 3600)
        val cal90kg = GpsViewModel.calculerCalories("cardio", 90.0, 3600)
        assertEquals(cal60kg * 1.5, cal90kg, 0.001)
    }

    @Test
    fun `calculerCalories proportionnel a la duree`() {
        val cal30min = GpsViewModel.calculerCalories("cardio", 70.0, 1800)
        val cal60min = GpsViewModel.calculerCalories("cardio", 70.0, 3600)
        assertEquals(cal30min * 2.0, cal60min, 0.001)
    }
}
