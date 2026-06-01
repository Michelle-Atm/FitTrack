package com.example.fitrack.viewmodel

import android.hardware.SensorEventListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
class SensorViewModelTest {

    private val testDispatcher = UnconfinedTestDispatcher()

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // ─── Baseline ────────────────────────────────────────────────────────────

    @Test
    fun `etat initial - inactif, zero pas, zero minutes`() {
        val vm = SensorViewModel(FakeSensorBridge())
        assertFalse(vm.estActif.value)
        assertEquals(0, vm.pasAujourdhui.value)
        assertEquals(0, vm.dureeActiveMinutes.value)
    }

    @Test
    fun `si capteur indisponible, demarrer ne change pas etat`() {
        val bridge = FakeSensorBridge(disponible = false)
        val vm = SensorViewModel(bridge)
        vm.toggleActif()
        assertFalse(vm.estActif.value)
        assertFalse(bridge.inscrit)
    }

    @Test
    fun `toggleActif active le capteur et met estActif a true`() {
        val bridge = FakeSensorBridge()
        val vm = SensorViewModel(bridge)
        vm.toggleActif()
        assertTrue(vm.estActif.value)
        assertTrue(bridge.inscrit)
    }

    // ─── Comptage des pas ────────────────────────────────────────────────────

    @Test
    fun `premier evenement etablit la baseline et donne zero pas`() {
        val vm = SensorViewModel(FakeSensorBridge())
        vm.toggleActif()
        vm.traiterEvenementPas(5_000)
        assertEquals(0, vm.pasAujourdhui.value)
    }

    @Test
    fun `evenements suivants comptent les pas depuis la baseline`() {
        val vm = SensorViewModel(FakeSensorBridge())
        vm.toggleActif()
        vm.traiterEvenementPas(5_000)
        vm.traiterEvenementPas(5_050)
        assertEquals(50, vm.pasAujourdhui.value)
        vm.traiterEvenementPas(5_200)
        assertEquals(200, vm.pasAujourdhui.value)
    }

    @Test
    fun `les pas s accumulent correctement sur plusieurs evenements`() {
        val vm = SensorViewModel(FakeSensorBridge())
        vm.toggleActif()
        vm.traiterEvenementPas(1_000)  // baseline
        vm.traiterEvenementPas(1_100)
        vm.traiterEvenementPas(1_250)
        vm.traiterEvenementPas(1_300)
        assertEquals(300, vm.pasAujourdhui.value)
    }

    // ─── Arrêt / nettoyage ───────────────────────────────────────────────────

    @Test
    fun `arreter desactive et desinscrit le capteur`() {
        val bridge = FakeSensorBridge()
        val vm = SensorViewModel(bridge)
        vm.toggleActif()
        assertTrue(bridge.inscrit)
        vm.toggleActif()
        assertFalse(vm.estActif.value)
        assertFalse(bridge.inscrit)
    }

    @Test
    fun `arret fige la duree calculee depuis le debut de session`() {
        val clock = FakeClock(0L)
        val vm = SensorViewModel(FakeSensorBridge(), clock::maintenant)
        vm.toggleActif()
        clock.avancer(120_000L) // +2 minutes
        vm.toggleActif()
        assertEquals(2, vm.dureeActiveMinutes.value)
    }

    @Test
    fun `le timer met a jour la duree pendant la session`() = runTest {
        val clock = FakeClock(0L)
        val vm = SensorViewModel(FakeSensorBridge(), clock::maintenant)
        vm.toggleActif()
        clock.avancer(60_000L)
        advanceTimeBy(60_001L)
        assertEquals(1, vm.dureeActiveMinutes.value)
    }

    @Test
    fun `onCleared arrete le capteur et le timer`() {
        val bridge = FakeSensorBridge()
        val vm = SensorViewModel(bridge)
        vm.toggleActif()
        assertTrue(bridge.inscrit)
        vm.onCleared()
        assertFalse(vm.estActif.value)
        assertFalse(bridge.inscrit)
    }

    @Test
    fun `la baseline se reinitialise a chaque nouvelle session`() {
        val vm = SensorViewModel(FakeSensorBridge())
        // Session 1
        vm.toggleActif()
        vm.traiterEvenementPas(1_000)
        vm.traiterEvenementPas(1_080)
        assertEquals(80, vm.pasAujourdhui.value)
        vm.toggleActif()
        // Session 2 — baseline repart de zéro
        vm.toggleActif()
        vm.traiterEvenementPas(2_000)  // nouvelle baseline
        vm.traiterEvenementPas(2_030)
        assertEquals(30, vm.pasAujourdhui.value)
    }
}

// ─── Fakes ───────────────────────────────────────────────────────────────────

private class FakeSensorBridge(override val disponible: Boolean = true) : SensorBridge {
    var inscrit = false

    override fun inscrire(listener: SensorEventListener): Boolean {
        inscrit = true
        return disponible
    }

    override fun desinscrire(listener: SensorEventListener) {
        inscrit = false
    }
}

private class FakeClock(private var tempsMs: Long = 0L) {
    fun maintenant() = tempsMs
    fun avancer(ms: Long) { tempsMs += ms }
}
