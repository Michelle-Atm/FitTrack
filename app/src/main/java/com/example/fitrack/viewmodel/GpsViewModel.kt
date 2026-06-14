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
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

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

    private val _positionActuelle = MutableStateFlow<LatLng?>(null)
    val positionActuelle: StateFlow<LatLng?> = _positionActuelle.asStateFlow()

    private val _sauvegardeEnCours = MutableStateFlow(false)
    val sauvegardeEnCours: StateFlow<Boolean> = _sauvegardeEnCours.asStateFlow()

    private val _erreurSauvegarde = MutableStateFlow<String?>(null)
    val erreurSauvegarde: StateFlow<String?> = _erreurSauvegarde.asStateFlow()

    private var debutEnregistrement: Long = 0L
    private var timerJob: Job? = null

    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 2_000L)
        .setMinUpdateIntervalMillis(1_000L)
        .build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(result: LocationResult) {
            val location = result.lastLocation ?: return
            // Ignore locations with poor accuracy (> 25m) to avoid GPS jitter
            if (location.hasAccuracy() && location.accuracy > 25f) return
            val nouveauPoint = LatLng(location.latitude, location.longitude)
            val trajet = _trajetGps.value

            if (trajet.isNotEmpty()) {
                val mesure = FloatArray(1)
                Location.distanceBetween(
                    trajet.last().latitude, trajet.last().longitude,
                    nouveauPoint.latitude, nouveauPoint.longitude,
                    mesure
                )
                // Ignore jumps > 200m in 2s (GPS bounce artifact)
                if (mesure[0] <= 200f) {
                    _distanceTotale.value += mesure[0] / 1_000f
                }
            }

            _vitesseCourante.value =
                if (location.hasSpeed()) location.speed * 3.6f else 0f

            _positionActuelle.value = nouveauPoint
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

    fun reinitialiserErreurSauvegarde() { _erreurSauvegarde.value = null }

    fun terminerEtSauvegarder(userId: String, typeSeance: String = "cardio", poidsKg: Double = 70.0) {
        if (_sauvegardeEnCours.value) return
        val distance = _distanceTotale.value
        val duree = _dureeSecondes.value
        val seance = Seance(
            userId = userId,
            date = System.currentTimeMillis(),
            dureeMinutes = maxOf(1, duree / 60),
            type = typeSeance,
            caloriesDepensees = calculerCalories(typeSeance, poidsKg, duree)
        )
        arreterEnregistrement()
        _sauvegardeEnCours.value = true
        viewModelScope.launch {
            objectifRepository.ajouterSeance(seance)
                .onSuccess {
                    _trajetGps.value = emptyList()
                    _distanceTotale.value = 0f
                    _dureeSecondes.value = 0
                }
                .onFailure { e ->
                    _erreurSauvegarde.value = e.message ?: "Impossible de sauvegarder la séance"
                }
            _sauvegardeEnCours.value = false
        }
    }

    override fun onCleared() {
        arreterEnregistrement()
    }

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

        fun calculerCalories(typeSeance: String, poidsKg: Double, dureeSecondes: Int): Double {
            val dureeHeures = dureeSecondes.toDouble() / 3600.0
            return (metFacteur(typeSeance) * poidsKg * dureeHeures).coerceAtLeast(0.0)
        }

        fun factory(application: Application): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    GpsViewModel(application, FirestoreObjectifRepository()) as T
            }
    }
}
