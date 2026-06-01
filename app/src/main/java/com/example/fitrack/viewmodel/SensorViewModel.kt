package com.example.fitrack.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SensorViewModel(application: Application) : AndroidViewModel(application), SensorEventListener {

    private val sensorManager =
        application.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val capteurPas: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_STEP_COUNTER)

    private val _pasAujourdhui = MutableStateFlow(0)
    val pasAujourdhui: StateFlow<Int> = _pasAujourdhui.asStateFlow()

    private val _estActif = MutableStateFlow(false)
    val estActif: StateFlow<Boolean> = _estActif.asStateFlow()

    private val _dureeActiveMinutes = MutableStateFlow(0)
    val dureeActiveMinutes: StateFlow<Int> = _dureeActiveMinutes.asStateFlow()

    // Null tant que le premier event capteur n'est pas reçu
    private var baselinePas: Int? = null
    private var debutSession: Long = 0L
    private var timerJob: Job? = null

    fun toggleActif() {
        if (_estActif.value) arreter() else demarrer()
    }

    private fun demarrer() {
        if (capteurPas == null) return
        baselinePas = null
        debutSession = SystemClock.elapsedRealtime()
        _estActif.value = true
        sensorManager.registerListener(this, capteurPas, SensorManager.SENSOR_DELAY_NORMAL)
        timerJob = viewModelScope.launch {
            while (true) {
                delay(60_000L)
                _dureeActiveMinutes.value =
                    ((SystemClock.elapsedRealtime() - debutSession) / 60_000L).toInt()
            }
        }
    }

    private fun arreter() {
        sensorManager.unregisterListener(this)
        timerJob?.cancel()
        _estActif.value = false
        // Figer la durée finale
        if (debutSession > 0L) {
            _dureeActiveMinutes.value =
                ((SystemClock.elapsedRealtime() - debutSession) / 60_000L).toInt()
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type != Sensor.TYPE_STEP_COUNTER) return
        val rawSteps = event.values[0].toInt()
        // Premier event → définir la baseline pour compter depuis 0
        if (baselinePas == null) baselinePas = rawSteps
        _pasAujourdhui.value = rawSteps - (baselinePas ?: rawSteps)
    }

    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) {}

    override fun onCleared() {
        arreter()
    }
}
