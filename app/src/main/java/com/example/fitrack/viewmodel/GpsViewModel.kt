package com.example.fitrack.viewmodel

import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Stub temporaire — à remplacer dès que P2 merge GpsViewModel Sprint 2
class GpsViewModel : ViewModel() {

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

    fun toggleEnregistrement() {
        _estEnregistrement.value = !_estEnregistrement.value
    }

    fun terminerEtSauvegarder() {
        _estEnregistrement.value = false
    }
}
