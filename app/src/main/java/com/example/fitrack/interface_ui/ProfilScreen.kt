package com.example.fitrack.interface_ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Person
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
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.fitrack.components.StatCard
import com.example.fitrack.ui.theme.AmberFit
import com.example.fitrack.ui.theme.Border
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
    val keyboardController = LocalSoftwareKeyboardController.current

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
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Profil",
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                ),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = CardBG),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.04f))
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(VioletFit.copy(alpha = 0.15f))
                            .border(1.5.dp, VioletFit, CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Person,
                            contentDescription = null,
                            tint = VioletFit,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = baseUser.nom.ifBlank { baseUser.email.split("@").first() },
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Text(
                            text = "LEVEL ${baseUser.niveau} · ${if (baseUser.niveau < 3) "Apprenti" else "Expert"}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontStyle = androidx.compose.ui.text.font.FontStyle.Normal,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            ),
                            color = VioletFit
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { xpProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(99.dp)),
                            color = VioletFit,
                            trackColor = Color.White.copy(alpha = 0.05f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "${baseUser.xp % 500} / 500 XP",
                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                            color = TextDim
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = "IMC actuel",
                    value = if (imc > 0) "%.1f".format(imc) else "—",
                    accent = imcColor,
                    modifier = Modifier.weight(1f)
                )
                StatCard(
                    label = "Objectif",
                    value = goalOptions.firstOrNull { it.first == objectif }?.second ?: "—",
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatCard(
                    label = "Streak",
                    value = "0",
                    unit = "jours",
                    icon = Icons.Filled.LocalFireDepartment,
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

            Spacer(modifier = Modifier.height(32.dp))

            Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(Color.White.copy(alpha = 0.05f)))

            Spacer(modifier = Modifier.height(24.dp))

            Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {

                Column {
                    SectionLabel("OBJECTIF PRINCIPAL")
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        goalOptions.forEach { (id, label) ->
                            val sel = id == objectif
                            FilterChip(
                                selected = sel,
                                onClick = { objectif = id },
                                label = { Text(label, fontSize = 12.sp, fontWeight = FontWeight.Bold) },
                                shape = RoundedCornerShape(14.dp),
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
                }

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    OutlinedTextField(
                        value = poids, onValueChange = { poids = it },
                        label = { Text("Poids", fontWeight = FontWeight.Medium) },
                        suffix = { Text("kg", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
                        colors = fieldColorsProfile()
                    )
                    OutlinedTextField(
                        value = taille, onValueChange = { taille = it },
                        label = { Text("Taille", fontWeight = FontWeight.Medium) },
                        suffix = { Text("cm", fontWeight = FontWeight.Bold) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
                        keyboardActions = KeyboardActions(onDone = { keyboardController?.hide() }),
                        colors = fieldColorsProfile()
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = CardBG),
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.05f))
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp).fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("IMC RECALCULÉ", style = MaterialTheme.typography.labelSmall, color = TextDim, letterSpacing = 1.sp)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(viewModel.categorieIMC(imc), style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                        }
                        Text(
                            text = if (imc > 0) "%.1f".format(imc) else "—",
                            style = MaterialTheme.typography.headlineMedium.copy(
                                fontWeight = FontWeight.ExtraBold, letterSpacing = (-0.5).sp
                            ),
                            color = imcColor
                        )
                    }
                }

                Column {
                    SectionLabel("ALLERGIES")
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        allergies.forEach { a ->
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CardBG)
                                    .border(1.dp, Border, RoundedCornerShape(12.dp))
                                    .padding(start = 12.dp, top = 4.dp, bottom = 4.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(a, style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold), color = Color.White)
                                IconButton(
                                    onClick = { allergies = allergies - a },
                                    modifier = Modifier.size(28.dp)
                                ) {
                                    Icon(Icons.Filled.Close, null, tint = DangerFit, modifier = Modifier.size(14.dp))
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = newAllergy,
                        onValueChange = { newAllergy = it },
                        placeholder = { Text("Ajouter une allergie...", color = TextDim) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = fieldColorsProfile(),
                        trailingIcon = {
                            IconButton(
                                onClick = {
                                    if (newAllergy.isNotBlank()) {
                                        allergies = allergies + newAllergy.trim()
                                        newAllergy = ""
                                    }
                                }
                            ) {
                                Icon(Icons.Filled.Add, null, tint = MintFit)
                            }
                        }
                    )
                }

                Column {
                    SectionLabel("DISPONIBILITÉS HEBDOMADAIRES")
                    Spacer(modifier = Modifier.height(10.dp))
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
                                    .height(44.dp),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (sel) MintFit else CardBG,
                                    contentColor = if (sel) Color(0xFF002817) else TextDim
                                ),
                                shape = RoundedCornerShape(14.dp),
                                border = BorderStroke(1.dp, if (sel) MintFit else Border)
                            ) {
                                Text(label, fontSize = 13.sp, fontWeight = FontWeight.ExtraBold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val updated = baseUser.copy(
                            poids = poids.toDoubleOrNull() ?: baseUser.poids,
                            taille = taille.toDoubleOrNull()?.toInt() ?: taille.toIntOrNull() ?: baseUser.taille,
                            objectif = objectif,
                            allergies = allergies,
                            disponibilites = disponibilites
                        )
                        viewModel.mettreAJourProfil(updated)
                    },
                    enabled = uiState !is AuthViewModel.AuthUiState.Chargement,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MintFit,
                        disabledContainerColor = MintFit.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (uiState is AuthViewModel.AuthUiState.Chargement) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = Color(0xFF002817),
                            strokeWidth = 2.5.dp
                        )
                    } else {
                        Text(
                            "Enregistrer",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF002817)
                        )
                    }
                }

                Button(
                    onClick = { viewModel.deconnexion() },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.5.dp, DangerFit.copy(alpha = 0.3f))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.Logout, null, tint = DangerFit, modifier = Modifier.size(18.dp))
                        Text("Se déconnecter", color = DangerFit, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        color = TextDim
    )
}

@Composable
private fun fieldColorsProfile() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = Border,
    focusedBorderColor = MintFit,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White,
    unfocusedContainerColor = CardBG,
    focusedContainerColor = CardBG,
    focusedLabelColor = MintFit,
    unfocusedLabelColor = TextDim
)