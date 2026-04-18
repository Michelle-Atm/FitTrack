package com.example.fitrack.viewmodel

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class AuthViewModel : ViewModel() {
    // État pour savoir si on affiche le chargement
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    // Fonction de simulation de connexion (on branchera Firebase plus tard)
    fun login(email: String, mdp: String) {
        _isLoading.value = true
        // Ici viendra la logique Firebase Auth
    }
}