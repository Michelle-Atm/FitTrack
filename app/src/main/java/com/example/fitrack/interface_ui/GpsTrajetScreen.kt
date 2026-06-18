package com.example.fitrack.interface_ui

import android.Manifest
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.example.fitrack.components.PermissionBox
import com.example.fitrack.ui.theme.AmberFit
import com.example.fitrack.ui.theme.CardBG
import com.example.fitrack.ui.theme.DangerFit
import com.example.fitrack.ui.theme.DarkBG
import com.example.fitrack.ui.theme.DarkText
import com.example.fitrack.ui.theme.MintFit
import com.example.fitrack.ui.theme.TextDim
import com.example.fitrack.viewmodel.GpsViewModel

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun GpsTrajetScreen(gpsViewModel: GpsViewModel, userId: String = "") {
    val locationPermissions = rememberMultiplePermissionsState(
        listOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    if (!locationPermissions.permissions.all { it.status.isGranted }) {
        PermissionBox(
            message = "La localisation est nécessaire pour enregistrer votre trajet GPS.",
            bouton = "Autoriser",
            onClick = { locationPermissions.launchMultiplePermissionRequest() }
        )
        return
    }

    val trajetGps by gpsViewModel.trajetGps.collectAsStateWithLifecycle()
    val estEnregistrement by gpsViewModel.estEnregistrement.collectAsStateWithLifecycle()
    val distanceTotale by gpsViewModel.distanceTotale.collectAsStateWithLifecycle()
    val vitesseCourante by gpsViewModel.vitesseCourante.collectAsStateWithLifecycle()
    val dureeSecondes by gpsViewModel.dureeSecondes.collectAsStateWithLifecycle()
    val positionActuelle by gpsViewModel.positionActuelle.collectAsStateWithLifecycle()
    val sauvegardeEnCours by gpsViewModel.sauvegardeEnCours.collectAsStateWithLifecycle()
    val erreurSauvegarde by gpsViewModel.erreurSauvegarde.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(erreurSauvegarde) {
        val err = erreurSauvegarde ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(err)
        gpsViewModel.reinitialiserErreurSauvegarde()
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            trajetGps.lastOrNull() ?: positionActuelle ?: LatLng(0.0, 0.0),
            if (positionActuelle != null || trajetGps.isNotEmpty()) 15f else 2f
        )
    }

    val dureeLabel = "%02d:%02d".format(dureeSecondes / 60, dureeSecondes % 60)

    Scaffold(
        containerColor = DarkBG,
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(
                    snackbarData = data,
                    containerColor = DangerFit,
                    contentColor = Color.White
                )
            }
        }
    ) { innerPadding ->
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBG)
            .padding(innerPadding)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.65f)
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState
            ) {
                if (trajetGps.isNotEmpty()) {
                    Polyline(
                        points = trajetGps,
                        color = MintFit,
                        width = 8f
                    )
                    Marker(
                        state = MarkerState(trajetGps.first()),
                        title = "Départ"
                    )
                    Marker(
                        state = MarkerState(trajetGps.last()),
                        title = "Position actuelle",
                        icon = BitmapDescriptorFactory.defaultMarker(
                            BitmapDescriptorFactory.HUE_GREEN
                        )
                    )
                }
            }
            if (trajetGps.isEmpty() && !estEnregistrement) {
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 12.dp)
                        .background(Color.Black.copy(alpha = 0.55f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "Appuyez sur Start pour commencer l'enregistrement",
                        color = Color.White,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.35f)
                .background(CardBG)
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatBloc(
                    label = "Distance",
                    valeur = "%.2f km".format(distanceTotale),
                    accent = MintFit
                )
                StatBloc(
                    label = "Vitesse",
                    valeur = "%.1f km/h".format(vitesseCourante),
                    accent = AmberFit
                )
                StatBloc(
                    label = "Durée",
                    valeur = dureeLabel,
                    accent = Color.White
                )
            }

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = { gpsViewModel.toggleEnregistrement() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (estEnregistrement) DangerFit else MintFit
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    androidx.compose.material3.Icon(
                        imageVector = if (estEnregistrement) Icons.Default.Stop
                                      else Icons.Default.FiberManualRecord,
                        contentDescription = null,
                        tint = if (estEnregistrement) Color.White else DarkText
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = if (estEnregistrement) "Stop" else "Start",
                        color = if (estEnregistrement) Color.White else DarkText,
                        fontWeight = FontWeight.Bold
                    )
                }
                if (trajetGps.isNotEmpty()) {
                    OutlinedButton(
                        onClick = { gpsViewModel.terminerEtSauvegarder(userId) },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !sauvegardeEnCours,
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.dp,
                            if (sauvegardeEnCours) MintFit.copy(alpha = 0.4f) else MintFit
                        )
                    ) {
                        if (sauvegardeEnCours) {
                            CircularProgressIndicator(
                                color = MintFit,
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(Modifier.width(8.dp))
                            Text("Sauvegarde...", color = MintFit.copy(alpha = 0.6f), fontWeight = FontWeight.Bold)
                        } else {
                            Text("Terminer et sauvegarder", color = MintFit, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
    }
}

@Composable
private fun StatBloc(label: String, valeur: String, accent: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = valeur,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 20.sp,
                letterSpacing = (-0.5).sp
            ),
            color = accent
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextDim
        )
    }
}
