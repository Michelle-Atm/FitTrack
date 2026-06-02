package com.example.fitrack.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.os.Looper
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.fitrack.model.Seance
import com.example.fitrack.repository.ObjectifRepository
import com.example.fitrack.repository.firestore.FirestoreObjectifRepository
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class GpsViewModel(
    application: Application,
    private val objectifRepository: ObjectifRepository
) : AndroidViewModel(application) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(application)

    private val _trajetGps = MutableStateFlow<List<LatLng>>(emptyList())
    val trajetGps: StateFlow<List<LatLng>> = _trajetGps.asStateFlow()

    private val _estEnregistrement = MutableStateFlow(false)
    val estEnregistrement: StateFlow<Boolean> = _estEnregistrement.asStateFlow()

    private val _distanceTotale = MutableStateFlow(0f)
    val distanceTotale: StateFlow<Float> = _distanceTotale.asStateFlow()

    private val _vitesseCourante = MutableStateFlow(0f)
    val vitesseCourante: StateFlow<Float> = _vitesseCourante.asStateFlow()

    private val _dureeSecondes = MutableStateFlow(0)
    val dureeSecondes: StateFlow<Int> = _dureeSecondes.asStateFlow()

    private var debutEnregistrement: Long = 0L
    private var timerJob: Job? = null

    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2_000L)
        .setMinUpdateIntervalMillis(1_000L)
        .build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            val nouveauPoint = LatLng(location.latitude, location.longitude)
            val trajet = _trajetGps.value

            if (trajet.isNotEmpty()) {
                val mesure = FloatArray(1)
                Location.distanceBetween(
                    trajet.last().latitude, trajet.last().longitude,
                    nouveauPoint.latitude, nouveauPoint.longitude,
                    mesure
                )
                _distanceTotale.value += mesure[0] / 1_000f
            }

            _vitesseCourante.value =
                if (location.hasSpeed()) location.speed * 3.6f else 0f

            _trajetGps.value = trajet + nouveauPoint
        }
    }

    // La permission est vérifiée dans GpsTrajetScreen avant tout appel
    @SuppressLint("MissingPermission")
    fun toggleEnregistrement() {
        if (_estEnregistrement.value) arreterEnregistrement() else demarrerEnregistrement()
    }

    @SuppressLint("MissingPermission")
    private fun demarrerEnregistrement() {
        _trajetGps.value = emptyList()
        _distanceTotale.value = 0f
        _vitesseCourante.value = 0f
        _dureeSecondes.value = 0
        debutEnregistrement = SystemClock.elapsedRealtime()
        _estEnregistrement.value = true
        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1_000L)
                _dureeSecondes.value =
                    ((SystemClock.elapsedRealtime() - debutEnregistrement) / 1_000L).toInt()
            }
        }
    }

    private fun arreterEnregistrement() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
        timerJob?.cancel()
        _estEnregistrement.value = false
        _vitesseCourante.value = 0f
    }

    fun terminerEtSauvegarder(userId: String, typeSeance: String = "cardio") {
        val distance = _distanceTotale.value
        val duree = _dureeSecondes.value
        val seance = Seance(
            userId = userId,
            date = System.currentTimeMillis(),
            dureeMinutes = maxOf(1, duree / 60),
            type = typeSeance,
            caloriesDepensees = (distance * 60.0).coerceAtLeast(0.0)
        )
        arreterEnregistrement()
        viewModelScope.launch {
            objectifRepository.ajouterSeance(seance)
        }
        _trajetGps.value = emptyList()
        _distanceTotale.value = 0f
        _dureeSecondes.value = 0
    }

    override fun onCleared() {
        arreterEnregistrement()
    }

    companion object {
        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    GpsViewModel(application, FirestoreObjectifRepository()) as T
            }
    }
}
