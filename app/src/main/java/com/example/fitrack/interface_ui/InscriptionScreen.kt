package com.example.fitrack.interface_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitrack.model.User
import com.example.fitrack.ui.theme.AmberFit
import com.example.fitrack.ui.theme.CardBG
import com.example.fitrack.ui.theme.DarkBG
import com.example.fitrack.ui.theme.MintFit
import com.example.fitrack.ui.theme.TextDim
import com.example.fitrack.ui.theme.VioletFit
import com.example.fitrack.viewmodel.AuthViewModel

private data class GoalOption(val id: String, val label: String, val sub: String)

private val goalOptions = listOf(
    GoalOption("perte_poids",  "Perte de poids",   "Déficit calorique"),
    GoalOption("prise_masse",  "Prise de masse",   "Surplus protéiné"),
    GoalOption("endurance",    "Endurance & Cardio","Capacité aérobie"),
    GoalOption("remise_forme", "Remise en forme",  "Retour progressif"),
    GoalOption("maintien",     "Maintien",         "Stabiliser les acquis"),
)

private val levelOptions = listOf(
    "debutant" to "Débutant",
    "intermediaire" to "Intermédiaire",
    "avance" to "Avancé"
)

@Composable
fun InscriptionScreen(
    viewModel: AuthViewModel,
    onRetourLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val focusManager = LocalFocusManager.current

    var step by remember { mutableStateOf(1) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var nom by remember { mutableStateOf("") }
    var objectif by remember { mutableStateOf<String?>(null) }
    var level by remember { mutableStateOf<String?>(null) }
    var poids by remember { mutableStateOf("") }
    var taille by remember { mutableStateOf("") }

    val isLoading = uiState is AuthViewModel.AuthUiState.Chargement
    val errorMessage = (uiState as? AuthViewModel.AuthUiState.Erreur)?.message

    val imc = remember(poids, taille) {
        val p = poids.toDoubleOrNull() ?: 0.0
        val t = taille.toIntOrNull() ?: 0
        if (p > 0 && t > 0) p / ((t / 100.0) * (t / 100.0)) else null
    }
    val imcFormatted = imc?.let { "%.1f".format(it) }
    val imcColor = when {
        imc == null -> TextDim
        imc < 18.5 -> AmberFit
        imc < 25.0 -> MintFit
        imc < 30.0 -> AmberFit
        else -> Color(0xFFE35C5C)
    }
    val imcLabel = when {
        imc == null -> "Renseigne tes données"
        imc < 18.5 -> "Insuffisant"
        imc < 25.0 -> "Poids normal"
        imc < 30.0 -> "Surpoids"
        else -> "Obésité"
    }

    val stepOk = when (step) {
        1 -> email.contains("@") && password.length >= 4 && nom.length >= 2
        2 -> objectif != null && level != null
        3 -> poids.toDoubleOrNull() != null && taille.toIntOrNull() != null
        else -> false
    }

    LaunchedEffect(uiState) {
        if (uiState is AuthViewModel.AuthUiState.Erreur) {
            // keep current step, error shown below button
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBG)
            .imePadding()
    ) {
        // Header + progress
        Column(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(
                    onClick = { if (step > 1) step-- else onRetourLogin() },
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Étape $step / 3",
                    style = MaterialTheme.typography.titleLarge,
                    color = Color.White
                )
            }
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                repeat(3) { i ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(4.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(if (i < step) MintFit else Color.White.copy(alpha = 0.08f))
                    )
                }
            }
        }

        // Scrollable content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            when (step) {
                1 -> Step1(
                    email = email, onEmailChange = { email = it; viewModel.reinitialiserEtat() },
                    password = password, onPasswordChange = { password = it; viewModel.reinitialiserEtat() },
                    passwordVisible = passwordVisible, onToggleVisibility = { passwordVisible = !passwordVisible },
                    nom = nom, onNomChange = { nom = it; viewModel.reinitialiserEtat() },
                    focusManager = focusManager
                )
                2 -> Step2(
                    objectif = objectif, onObjectifChange = { objectif = it },
                    level = level, onLevelChange = { level = it }
                )
                3 -> Step3(
                    poids = poids, onPoidsChange = { poids = it },
                    taille = taille, onTailleChange = { taille = it },
                    imcFormatted = imcFormatted, imcLabel = imcLabel, imcColor = imcColor,
                    focusManager = focusManager
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Bottom bar
        Column(
            modifier = Modifier
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 12.dp)
        ) {
            if (errorMessage != null) {
                Text(
                    text = errorMessage,
                    color = Color.Red,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )
            }
            Button(
                onClick = {
                    focusManager.clearFocus()
                    if (step < 3) {
                        step++
                    } else {
                        val user = User(
                            nom = nom,
                            objectif = objectif ?: "",
                            experience = level ?: "debutant",
                            poids = poids.toDoubleOrNull() ?: 0.0,
                            taille = taille.toIntOrNull() ?: 0,
                            disponibilites = emptyList(),
                            allergies = emptyList()
                        )
                        viewModel.inscrire(email.trim(), password, user)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                enabled = stepOk && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintFit,
                    disabledContainerColor = MintFit.copy(alpha = 0.45f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        color = Color(0xFF002817),
                        modifier = Modifier.size(24.dp),
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (step < 3) "Continuer" else "Créer mon compte",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF002817)
                    )
                }
            }
        }
    }
}

@Composable
private fun Step1(
    email: String, onEmailChange: (String) -> Unit,
    password: String, onPasswordChange: (String) -> Unit,
    passwordVisible: Boolean, onToggleVisibility: () -> Unit,
    nom: String, onNomChange: (String) -> Unit,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Text(
        text = "Crée ton compte",
        style = MaterialTheme.typography.headlineMedium,
        color = Color.White,
        modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
    )
    Text(
        text = "On commence par les bases.",
        style = MaterialTheme.typography.bodyMedium,
        color = TextDim,
        modifier = Modifier.padding(bottom = 24.dp)
    )
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
        OutlinedTextField(
            value = email, onValueChange = onEmailChange, label = { Text("Email", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            singleLine = true, shape = RoundedCornerShape(8.dp),
            colors = fieldColors()
        )
        OutlinedTextField(
            value = password, onValueChange = onPasswordChange, label = { Text("Mot de passe", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff,
                        contentDescription = null, tint = Color.Gray
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            singleLine = true, shape = RoundedCornerShape(8.dp),
            colors = fieldColors()
        )
        OutlinedTextField(
            value = nom, onValueChange = onNomChange, label = { Text("Nom d'affichage", color = Color.Gray) },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            singleLine = true, shape = RoundedCornerShape(8.dp),
            colors = fieldColors()
        )
    }
}

@Composable
private fun Step2(
    objectif: String?, onObjectifChange: (String) -> Unit,
    level: String?, onLevelChange: (String) -> Unit
) {
    Text(
        text = "Quel est ton objectif ?",
        style = MaterialTheme.typography.headlineMedium,
        color = Color.White,
        modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
    )
    Text(
        text = "On personnalise ton programme.",
        style = MaterialTheme.typography.bodyMedium,
        color = TextDim,
        modifier = Modifier.padding(bottom = 20.dp)
    )
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        goalOptions.forEach { g ->
            val selected = g.id == objectif
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (selected) MintFit.copy(alpha = 0.07f) else CardBG)
                    .border(
                        width = if (selected) 1.5.dp else 1.dp,
                        color = if (selected) MintFit else Color.White.copy(alpha = 0.06f),
                        shape = RoundedCornerShape(12.dp)
                    )
                    .then(Modifier.padding(14.dp))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                ) {}
                TextButton(
                    onClick = { onObjectifChange(g.id) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(g.label, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.White)
                            Text(g.sub, style = MaterialTheme.typography.bodySmall, color = TextDim)
                        }
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(20.dp)
                                .clip(RoundedCornerShape(999.dp))
                                .background(if (selected) MintFit else Color.Transparent)
                                .border(
                                    1.5.dp,
                                    if (selected) MintFit else Color.White.copy(alpha = 0.10f),
                                    RoundedCornerShape(999.dp)
                                )
                        ) {}
                    }
                }
            }
        }
    }
    Spacer(modifier = Modifier.height(28.dp))
    Text(
        text = "TON NIVEAU D'EXPÉRIENCE ?",
        style = MaterialTheme.typography.labelSmall,
        color = TextDim,
        modifier = Modifier.padding(bottom = 10.dp)
    )
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        levelOptions.forEach { (id, label) ->
            val selected = id == level
            FilterChip(
                selected = selected,
                onClick = { onLevelChange(id) },
                label = { Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(999.dp),
                colors = FilterChipDefaults.filterChipColors(
                    selectedContainerColor = MintFit,
                    selectedLabelColor = Color(0xFF002817),
                    containerColor = CardBG,
                    labelColor = TextDim
                ),
                border = FilterChipDefaults.filterChipBorder(
                    selected = selected,
                    enabled = true,
                    selectedBorderColor = MintFit,
                    borderColor = Color.White.copy(alpha = 0.06f)
                )
            )
        }
    }
}

@Composable
private fun Step3(
    poids: String, onPoidsChange: (String) -> Unit,
    taille: String, onTailleChange: (String) -> Unit,
    imcFormatted: String?, imcLabel: String, imcColor: Color,
    focusManager: androidx.compose.ui.focus.FocusManager
) {
    Text(
        text = "Données corporelles",
        style = MaterialTheme.typography.headlineMedium,
        color = Color.White,
        modifier = Modifier.padding(top = 4.dp, bottom = 6.dp)
    )
    Text(
        text = "Pour calculer tes besoins.",
        style = MaterialTheme.typography.bodyMedium,
        color = TextDim,
        modifier = Modifier.padding(bottom = 24.dp)
    )
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            value = poids, onValueChange = onPoidsChange, label = { Text("Poids", color = Color.Gray) },
            suffix = { Text("kg", color = Color.Gray) },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next),
            keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
            singleLine = true, shape = RoundedCornerShape(8.dp),
            colors = fieldColors()
        )
        OutlinedTextField(
            value = taille, onValueChange = onTailleChange, label = { Text("Taille", color = Color.Gray) },
            suffix = { Text("cm", color = Color.Gray) },
            modifier = Modifier.weight(1f),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number, imeAction = ImeAction.Done),
            keyboardActions = KeyboardActions(onDone = { focusManager.clearFocus() }),
            singleLine = true, shape = RoundedCornerShape(8.dp),
            colors = fieldColors()
        )
    }
    Spacer(modifier = Modifier.height(20.dp))
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(CardBG)
            .border(1.dp, imcColor.copy(alpha = 0.2f), RoundedCornerShape(12.dp))
            .padding(22.dp)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = "INDICE DE MASSE CORPORELLE",
                style = MaterialTheme.typography.labelSmall,
                color = TextDim
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = imcFormatted ?: "—",
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 52.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-2).sp,
                    lineHeight = 52.sp
                ),
                color = if (imcFormatted != null) imcColor else TextDim
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = imcLabel,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                color = if (imcFormatted != null) imcColor else TextDim
            )
        }
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = Color.Gray,
    focusedBorderColor = VioletFit,
    focusedTextColor = Color.White,
    unfocusedTextColor = Color.White
)
