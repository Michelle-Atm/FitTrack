package com.example.fitrack.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

// Stub temporaire — à remplacer dès que P2 merge SensorViewModel Sprint 2
class SensorViewModel : ViewModel() {

    private val _pasAujourdhui = MutableStateFlow(0)
    val pasAujourdhui: StateFlow<Int> = _pasAujourdhui.asStateFlow()

    private val _estActif = MutableStateFlow(false)
    val estActif: StateFlow<Boolean> = _estActif.asStateFlow()

    private val _dureeActiveMinutes = MutableStateFlow(0)
    val dureeActiveMinutes: StateFlow<Int> = _dureeActiveMinutes.asStateFlow()

    fun toggleActif() {
        _estActif.value = !_estActif.value
    }
}
