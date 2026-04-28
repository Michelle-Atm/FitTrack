package com.example.fitrack.interface_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fitrack.components.StatCard
import com.example.fitrack.ui.theme.AmberFit
import com.example.fitrack.ui.theme.Border
import com.example.fitrack.ui.theme.BorderStr
import com.example.fitrack.ui.theme.CardBG
import com.example.fitrack.ui.theme.DangerFit
import com.example.fitrack.ui.theme.DarkBG
import com.example.fitrack.ui.theme.MintFit
import com.example.fitrack.ui.theme.TextDim
import com.example.fitrack.ui.theme.VioletFit
import com.example.fitrack.viewmodel.AuthViewModel

private val goalOptions = listOf(
    "perte_poids" to "Perte de poids",
    "prise_masse" to "Prise de masse",
    "endurance" to "Endurance",
    "remise_forme" to "Remise en forme",
    "maintien" to "Maintien"
)

private val weekdays = listOf("L", "Ma", "Me", "J", "V", "S", "D")
private val weekdayKeys = listOf("lundi", "mardi", "mercredi", "jeudi", "vendredi", "samedi", "dimanche")

@Composable
fun ProfilScreen(viewModel: AuthViewModel, navController: NavController) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val utilisateurActuel by viewModel.utilisateurActuel.collectAsStateWithLifecycle()

    val userCourant = when (val s = uiState) {
        is AuthViewModel.AuthUiState.Succes -> s.utilisateur
        else -> utilisateurActuel
    }

    if (userCourant == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = MintFit)
        }
        return
    }
    val baseUser = userCourant

    var poids by remember(baseUser.uid) { mutableStateOf(baseUser.poids.toString().trimEnd('0').trimEnd('.')) }
    var taille by remember(baseUser.uid) { mutableStateOf(baseUser.taille.toString()) }
    var objectif by remember(baseUser.uid) { mutableStateOf(baseUser.objectif) }
    var allergies by remember(baseUser.uid) { mutableStateOf(baseUser.allergies) }
    var disponibilites by remember(baseUser.uid) { mutableStateOf(baseUser.disponibilites) }
    var newAllergy by remember { mutableStateOf("") }

    val imc = remember(poids, taille) {
        val p = poids.toDoubleOrNull() ?: 0.0
        val t = taille.toIntOrNull() ?: 0
        viewModel.calculerIMC(p, t)
    }
    val imcColor = when {
        imc <= 0 -> TextDim
        imc < 18.5 -> AmberFit
        imc < 25.0 -> MintFit
        imc < 30.0 -> AmberFit
        else -> DangerFit
    }
    val xpProgress = (baseUser.xp % 500).toFloat() / 500f

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.evenements.collect { evenement ->
            when (evenement) {
                is AuthViewModel.AuthEvenement.ProfilMisAJour ->
                    snackbarHostState.showSnackbar(
                        message = "Profil enregistré ✓",
                        duration = SnackbarDuration.Short
                    )
                is AuthViewModel.AuthEvenement.Erreur ->
                    snackbarHostState.showSnackbar(
                        message = evenement.message,
                        duration = SnackbarDuration.Long
                    )
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBG,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBG)
                .verticalScroll(rememberScrollState())
                .padding(innerPadding)
                .padding(bottom = 32.dp)
        ) {
            Text(
                text = "Profil",
                style = MaterialTheme.typography.headlineMedium,
                color = Color.White,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )

            // Avatar + XP
            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .size(72.dp)
                        .clip(CircleShape)
                        .background(VioletFit)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Pets,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = baseUser.nom.ifBlank { baseUser.email },
                        style = MaterialTheme.typography.titleLarge,
                        color = Color.White
                    )
                    Text(
                        text = "NIVEAU ${baseUser.niveau} · ${if (baseUser.niveau < 3) "Apprenti" else "Expert"}",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = VioletFit
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { xpProgress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(999.dp)),
                        color = VioletFit,
                        trackColor = Color.White.copy(alpha = 0.06f)
                    )
                    Text(
                        text = "${baseUser.xp % 500} / 500 XP",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextDim
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Stats grid 2x2
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    label = "IMC actuel",
                    value = if (imc > 0) "%.1f".format(imc) else "—",
                    accent = imcColor,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Objectif",
                    value = goalOptions.firstOrNull { it.first == objectif }?.second?.split(" ")?.firstOrNull() ?: "—",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row(
                modifier = Modifier.padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatCard(
                    label = "Streak",
                    value = "0",
                    unit = "jours",
                    icon = Icons.Filled.Pets,
                    iconColor = AmberFit,
                    accent = AmberFit,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Score hebdo",
                    value = "${baseUser.xp}",
                    unit = "pts",
                    accent = VioletFit,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Editable section
            Column(
                modifier = Modifier.padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(20.dp)
            ) {
                SectionLabel("MON PROFIL")

                // Objectif chips
                SectionLabel("OBJECTIF")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    goalOptions.forEach { (id, label) ->
                        val sel = id == objectif
                        androidx.compose.material3.FilterChip(
                            selected = sel,
                            onClick = { objectif = id },
                            label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                            shape = RoundedCornerShape(999.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MintFit,
                                selectedLabelColor = Color(0xFF002817),
                                containerColor = CardBG,
                                labelColor = TextDim
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                selected = sel, enabled = true,
                                selectedBorderColor = MintFit,
                                borderColor = Border
                            )
                        )
                    }
                }

                // Poids / Taille
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = poids, onValueChange = { poids = it },
                        label = { Text("Poids", color = Color.Gray) },
                        suffix = { Text("kg", color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        singleLine = true, shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = fieldColorsProfile()
                    )
                    OutlinedTextField(
                        value = taille, onValueChange = { taille = it },
                        label = { Text("Taille", color = Color.Gray) },
                        suffix = { Text("cm", color = Color.Gray) },
                        modifier = Modifier.weight(1f),
                        singleLine = true, shape = RoundedCornerShape(8.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = fieldColorsProfile()
                    )
                }

                // IMC card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBG),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Border)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("IMC RECALCULÉ", style = MaterialTheme.typography.labelSmall, color = TextDim)
                            Text(viewModel.categorieIMC(imc), style = MaterialTheme.typography.bodySmall, color = TextDim)
                        }
                        Text(
                            text = if (imc > 0) "%.1f".format(imc) else "—",
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontSize = 24.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp
                            ),
                            color = imcColor
                        )
                    }
                }

                // Allergies
                SectionLabel("ALLERGIES")
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    allergies.forEach { a ->
                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(999.dp))
                                .background(CardBG)
                                .border(1.dp, Border, RoundedCornerShape(999.dp))
                                .padding(start = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(a, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold), color = Color.White)
                            IconButton(
                                onClick = { allergies = allergies - a },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(Icons.Filled.Close, null, tint = TextDim, modifier = Modifier.size(11.dp))
                            }
                        }
                    }
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .border(1.dp, BorderStr, RoundedCornerShape(999.dp))
                            .padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = newAllergy,
                            onValueChange = { newAllergy = it },
                            placeholder = { Text("Ajouter", color = TextDim, fontSize = 12.sp) },
                            modifier = Modifier.size(width = 88.dp, height = 40.dp),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                unfocusedBorderColor = Color.Transparent,
                                focusedBorderColor = Color.Transparent,
                                unfocusedTextColor = Color.White, focusedTextColor = Color.White
                            )
                        )
                        IconButton(
                            onClick = {
                                if (newAllergy.isNotBlank()) {
                                    allergies = allergies + newAllergy.trim()
                                    newAllergy = ""
                                }
                            },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(Icons.Filled.Add, null, tint = TextDim, modifier = Modifier.size(12.dp))
                        }
                    }
                }

                // Disponibilités
                SectionLabel("DISPONIBILITÉS")
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    weekdays.forEachIndexed { i, label ->
                        val key = weekdayKeys[i]
                        val sel = key in disponibilites
                        Button(
                            onClick = {
                                disponibilites = if (sel) disponibilites - key else disponibilites + key
                            },
                            modifier = Modifier
                                .weight(1f)
                                .size(40.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (sel) MintFit else CardBG,
                                contentColor = if (sel) Color(0xFF002817) else TextDim
                            ),
                            shape = RoundedCornerShape(10.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, if (sel) MintFit else Border)
                        ) {
                            Text(label, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                        }
                    }
                }

                // Save button
                Button(
                    onClick = {
                        val updated = baseUser.copy(
                            poids = poids.toDoubleOrNull() ?: baseUser.poids,
                            taille = taille.toDoubleOrNull()?.toInt()
                                ?: taille.toIntOrNull()
                                ?: baseUser.taille,
                            objectif = objectif,
                            allergies = allergies,
                            disponibilites = disponibilites
                        )
                        viewModel.mettreAJourProfil(updated)
                    },
                    enabled = uiState !is AuthViewModel.AuthUiState.Chargement,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MintFit),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    if (uiState is AuthViewModel.AuthUiState.Chargement) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = Color(0xFF002817),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            "Enregistrer",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF002817)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Logout
                Button(
                    onClick = { viewModel.deconnexion() },
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.5.dp, DangerFit.copy(alpha = 0.4f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, tint = DangerFit, modifier = Modifier.size(16.dp))
                        Text("Se déconnecter", color = DangerFit, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(text, style = MaterialTheme.typography.labelSmall, color = TextDim, modifier = Modifier.padding(top = 4.dp))
}

@Composable
private fun fieldColorsProfile() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = Border,
    focusedBorderColor = MintFit,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    unfocusedContainerColor = CardBG,
    focusedContainerColor = CardBG
)
