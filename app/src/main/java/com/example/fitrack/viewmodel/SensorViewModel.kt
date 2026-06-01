package com.example.fitrack.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

interface SensorBridge {
    val disponible: Boolean
    fun inscrire(listener: SensorEventListener): Boolean
    fun desinscrire(listener: SensorEventListener)
}

class SensorViewModel(
    private val bridge: SensorBridge,
    private val clock: () -> Long = { SystemClock.elapsedRealtime() }
) : ViewModel(), SensorEventListener {

    private val _pasAujourdhui = MutableStateFlow(0)
    val pasAujourdhui: StateFlow<Int> = _pasAujourdhui.asStateFlow()

    private val _estActif = MutableStateFlow(false)
    val estActif: StateFlow<Boolean> = _estActif.asStateFlow()

    private val _dureeActiveMinutes = MutableStateFlow(0)
    val dureeActiveMinutes: StateFlow<Int> = _dureeActiveMinutes.asStateFlow()

    private var baselinePas: Int? = null
    private var debutSession: Long = 0L
    private var timerJob: Job? = null

    fun toggleActif() {
        if (_estActif.value) arreter() else demarrer()
    }

    private fun demarrer() {
        if (!bridge.disponible) return
        baselinePas = null
        debutSession = clock()
        _estActif.value = true
        bridge.inscrire(this)
        timerJob = viewModelScope.launch {
            while (true) {
                delay(60_000L)
                _dureeActiveMinutes.value = ((clock() - debutSession) / 60_000L).toInt()
            }
        }
    }

    private fun arreter() {
        bridge.desinscrire(this)
        timerJob?.cancel()
        _estActif.value = false
        if (debutSession > 0L) {
            _dureeActiveMinutes.value = ((clock() - debutSession) / 60_000L).toInt()
        }
    }

    // internal pour permettre les tests unitaires sans SensorEvent Android
    internal fun traiterEvenementPas(rawSteps: Int) {
        if (baselinePas == null) baselinePas = rawSteps
        _pasAujourdhui.value = rawSteps - (baselinePas ?: rawSteps)
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_STEP_COUNTER) return
        traiterEvenementPas(event.values[0].toInt())
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    public override fun onCleared() {
        arreter()
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    val sm = application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
                    val sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)
                    val bridge = object : SensorBridge {
                        override val disponible = sensor != null
                        override fun inscrire(listener: SensorEventListener) =
                            sm.registerListener(listener, sensor, SensorManager.SENSOR_DELAY_NORMAL)
                        override fun desinscrire(listener: SensorEventListener) =
                            sm.unregisterListener(listener)
                    }
                    return SensorViewModel(bridge) as T
                }
            }
    }
}
