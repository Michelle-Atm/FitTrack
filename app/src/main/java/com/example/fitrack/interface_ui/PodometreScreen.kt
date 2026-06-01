package com.example.fitrack.interface_ui

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.example.fitrack.components.PermissionBox
import com.example.fitrack.components.ProgressRing
import com.example.fitrack.components.StatCard
import com.example.fitrack.ui.theme.AmberFit
import com.example.fitrack.ui.theme.CardBG
import com.example.fitrack.ui.theme.DarkBG
import com.example.fitrack.ui.theme.DarkText
import com.example.fitrack.ui.theme.MintFit
import com.example.fitrack.ui.theme.TextDim
import com.example.fitrack.ui.theme.VioletFit
import com.example.fitrack.viewmodel.ObjectifViewModel
import com.example.fitrack.viewmodel.SensorViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun PodometreScreen(
    sensorViewModel: SensorViewModel,
    objectifViewModel: ObjectifViewModel
) {
    val permissionState = rememberPermissionState(Manifest.permission.ACTIVITY_RECOGNITION)

    if (!permissionState.status.isGranted) {
        PermissionBox(
            message = "L'accès au capteur de pas est nécessaire pour le podomètre.",
            bouton = "Autoriser",
            onClick = { permissionState.launchPermissionRequest() }
        )
        return
    }

    val pasAujourdhui by sensorViewModel.pasAujourdhui.collectAsStateWithLifecycle()
    val estActif by sensorViewModel.estActif.collectAsStateWithLifecycle()
    val dureeMinutes by sensorViewModel.dureeActiveMinutes.collectAsStateWithLifecycle()

    val objectifState by objectifViewModel.objectifUiState.collectAsStateWithLifecycle()
    val objectifPas = (objectifState as? ObjectifViewModel.ObjectifUiState.Succes)
        ?.progression?.objectif?.pasObjectif ?: 10000

    val calories = (pasAujourdhui * 0.04).toInt()
    val distanceKm = pasAujourdhui * 0.00075

    val dateLabel = SimpleDateFormat("EEEE dd MMMM", Locale.FRENCH)
        .format(Date())
        .replaceFirstChar { it.uppercase() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBG)
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(top = 12.dp, bottom = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Podomètre",
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White
                )
                Text(
                    text = dateLabel,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDim
                )
            }
            Button(
                onClick = { sensorViewModel.toggleActif() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (estActif) MintFit else CardBG
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (estActif) "Stop" else "Start",
                    color = if (estActif) DarkText else Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        Spacer(Modifier.height(32.dp))

        // Cercle de progression
        ProgressRing(
            value = pasAujourdhui.toDouble(),
            goal = objectifPas.toDouble(),
            color = MintFit,
            size = 220.dp
        ) {
            Text(
                text = pasAujourdhui.toString(),
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
                fontWeight = FontWeight.ExtraBold,
                fontSize = 52.sp
            )
            Text(
                text = "pas",
                style = MaterialTheme.typography.bodyMedium,
                color = TextDim
            )
            Text(
                text = "/ $objectifPas",
                style = MaterialTheme.typography.labelSmall,
                color = TextDim
            )
        }

        Spacer(Modifier.height(40.dp))

        // Stats 3 cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            StatCard(
                label = "Calories",
                value = calories.toString(),
                unit = "kcal",
                accent = AmberFit,
                icon = Icons.Default.LocalFireDepartment,
                iconColor = AmberFit,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Distance",
                value = "%.2f".format(distanceKm),
                unit = "km",
                accent = MintFit,
                icon = Icons.AutoMirrored.Filled.DirectionsWalk,
                iconColor = MintFit,
                modifier = Modifier.weight(1f)
            )
            StatCard(
                label = "Durée",
                value = dureeMinutes.toString(),
                unit = "min",
                accent = VioletFit,
                icon = Icons.Default.Timer,
                iconColor = VioletFit,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
