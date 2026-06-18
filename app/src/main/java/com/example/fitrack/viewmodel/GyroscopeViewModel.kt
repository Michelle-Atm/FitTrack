package com.example.fitrack.viewmodel

import android.app.Application
import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.sqrt

interface GyroscopeBridge {
    fun demarrer(onData: (x: Float, y: Float, z: Float) -> Unit)
    fun arreter()
    val disponible: Boolean
}

class CapteurGyroscopeBridge(context: Context) : GyroscopeBridge {

    private val sensorManager =
        context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gyroscope: Sensor? =
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)

    override val disponible: Boolean get() = gyroscope != null

    private var listener: SensorEventListener? = null

    override fun demarrer(onData: (x: Float, y: Float, z: Float) -> Unit) {
        if (gyroscope == null) return
        listener = object : SensorEventListener {
            override fun onSensorChanged(event: SensorEvent) {
                if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
                    onData(event.values[0], event.values[1], event.values[2])
                }
            }
            override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) = Unit
        }
        sensorManager.registerListener(listener, gyroscope, SensorManager.SENSOR_DELAY_NORMAL)
    }

    override fun arreter() {
        listener?.let { sensorManager.unregisterListener(it) }
        listener = null
    }
}

class GyroscopeViewModel(
    application: Application,
    private val bridge: GyroscopeBridge = CapteurGyroscopeBridge(application)
) : AndroidViewModel(application) {

    data class DonneesGyroscope(
        val x: Float = 0f,
        val y: Float = 0f,
        val z: Float = 0f
    ) {
        val intensite: Float get() = sqrt(x * x + y * y + z * z)
    }

    enum class TypeMouvement { STATIQUE, MARCHE, COURSE, VELO }

    private val _donnees = MutableStateFlow(DonneesGyroscope())
    val donnees: StateFlow<DonneesGyroscope> = _donnees.asStateFlow()

    private val _typeMouvement = MutableStateFlow(TypeMouvement.STATIQUE)
    val typeMouvement: StateFlow<TypeMouvement> = _typeMouvement.asStateFlow()

    private val _actif = MutableStateFlow(false)
    val actif: StateFlow<Boolean> = _actif.asStateFlow()

    val gyroscopeDisponible: Boolean get() = bridge.disponible

    fun demarrer() {
        if (!bridge.disponible) return
        _actif.value = true
        bridge.demarrer { x, y, z ->
            viewModelScope.launch {
                val d = DonneesGyroscope(x, y, z)
                _donnees.value = d
                _typeMouvement.value = GyroscopeViewModel.detecterMouvement(d.intensite)
            }
        }
    }

    fun arreter() {
        bridge.arreter()
        _actif.value = false
        _donnees.value = DonneesGyroscope()
        _typeMouvement.value = TypeMouvement.STATIQUE
    }

    override fun onCleared() = arreter()

    companion object {
        // Seuils en rad/s — calibrés empiriquement, exposés pour les tests unitaires
        fun detecterMouvement(intensite: Float): TypeMouvement = when {
            intensite < 0.15f -> TypeMouvement.STATIQUE
            intensite < 1.0f  -> TypeMouvement.MARCHE
            intensite < 2.5f  -> TypeMouvement.COURSE
            else              -> TypeMouvement.VELO  // rotation continue = vélo
        }

        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    GyroscopeViewModel(application) as T
            }
    }
}
