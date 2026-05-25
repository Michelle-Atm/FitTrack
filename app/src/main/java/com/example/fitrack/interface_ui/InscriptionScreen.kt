package com.example.fitrack.interface_ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitrack.model.User
import com.example.fitrack.ui.theme.*
import com.example.fitrack.viewmodel.AuthViewModel

private data class GoalOption(val id: String, val label: String, val sub: String)

private val goalOptions = listOf(
    GoalOption("perte_poids", "Perte de poids", "Déficit calorique"),
    GoalOption("prise_masse", "Prise de masse", "Surplus protéiné"),
    GoalOption("endurance", "Endurance", "Capacité aérobie"),
    GoalOption("remise_forme", "Remise en forme", "Retour progressif"),
    GoalOption("maintien", "Maintien", "Stabiliser les acquis"),
)

private val levelOptions = listOf(
    "debutant" to "Débutant",
    "intermediaire" to "Intermédiaire",
    "avance" to "Avancé"
)

@Composable
fun InscriptionScreen(viewModel: AuthViewModel, onRetourLogin: () -> Unit) {
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
        else -> DangerFit
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

    Scaffold(containerColor = DarkBG, contentWindowInsets = WindowInsets.safeDrawing) { innerPadding ->
        Column(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (step > 1) step-- else onRetourLogin() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                    }
                    Text("Étape $step / 3", style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold), color = Color.White)
                }
                Spacer(modifier = Modifier.height(16.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxWidth()) {
                    repeat(3) { i ->
                        Box(modifier = Modifier.weight(1f).height(4.dp).clip(RoundedCornerShape(99.dp)).background(if (i < step) MintFit else Color.White.copy(alpha = 0.05f)))
                    }
                }
            }

            Column(modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 24.dp)) {
                when (step) {
                    1 -> Step1(email, { email = it }, password, { password = it }, passwordVisible, { passwordVisible = !passwordVisible }, nom, { nom = it }, focusManager)
                    2 -> Step2(objectif, { objectif = it }, level, { level = it })
                    3 -> Step3(poids, { poids = it }, taille, { taille = it }, imcFormatted, imcLabel, imcColor, focusManager)
                }
            }

            Column(modifier = Modifier.padding(24.dp)) {
                if (errorMessage != null) {
                    Text(errorMessage, color = DangerFit, fontSize = 13.sp, modifier = Modifier.padding(bottom = 12.dp))
                }
                Button(
                    onClick = {
                        focusManager.clearFocus()
                        if (step < 3) step++ else {
                            val user = User(
                                nom = nom, objectif = objectif ?: "", niveau = 1, xp = 0,
                                poids = poids.toDoubleOrNull() ?: 0.0, taille = taille.toIntOrNull() ?: 0,
                                disponibilites = emptyList(), allergies = emptyList()
                            )
                            viewModel.inscrire(email.trim(), password, user)
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    enabled = !isLoading,
                    colors = ButtonDefaults.buttonColors(containerColor = MintFit),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isLoading) CircularProgressIndicator(color = Color(0xFF002817), modifier = Modifier.size(24.dp))
                    else Text(if (step < 3) "Continuer" else "Créer mon compte", fontWeight = FontWeight.ExtraBold, color = Color(0xFF002817))
                }
            }
        }
    }
}

@Composable
private fun Step1(email: String, onEmail: (String) -> Unit, pass: String, onPass: (String) -> Unit, visible: Boolean, onToggle: () -> Unit, nom: String, onNom: (String) -> Unit, fm: androidx.compose.ui.focus.FocusManager) {
    Text("Crée ton compte", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
    Spacer(modifier = Modifier.height(24.dp))
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Field(value = email, onValueChange = onEmail, label = "Email", type = KeyboardType.Email, action = ImeAction.Next) { fm.moveFocus(FocusDirection.Down) }
        OutlinedTextField(
            value = pass, onValueChange = onPass, label = { Text("Mot de passe") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = { IconButton(onClick = onToggle) { Icon(if (visible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff, null, tint = TextDim) } },
            shape = RoundedCornerShape(16.dp), colors = fieldColors(), singleLine = true
        )
        Field(value = nom, onValueChange = onNom, label = "Nom d'affichage", type = KeyboardType.Text, action = ImeAction.Done) { fm.clearFocus() }
    }
}

@Composable
private fun Step2(obj: String?, onObj: (String) -> Unit, lvl: String?, onLvl: (String) -> Unit) {
    Text("Objectif & Niveau", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
    Spacer(modifier = Modifier.height(24.dp))
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        goalOptions.forEach { g ->
            val sel = g.id == obj
            Card(
                onClick = { onObj(g.id) },
                colors = CardDefaults.cardColors(containerColor = if (sel) MintFit.copy(alpha = 0.1f) else CardBG),
                border = BorderStroke(1.dp, if (sel) MintFit else Color.White.copy(alpha = 0.05f)),
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(modifier = Modifier.padding(16.dp)) {
                    Column {
                        Text(g.label, fontWeight = FontWeight.Bold, color = Color.White)
                        Text(g.sub, style = MaterialTheme.typography.bodySmall, color = TextDim)
                    }
                }
            }
        }
    }
}

@Composable
private fun Step3(poids: String, onPoids: (String) -> Unit, taille: String, onTaille: (String) -> Unit, imc: String?, label: String, color: Color, fm: androidx.compose.ui.focus.FocusManager) {
    Text("Données corporelles", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold), color = Color.White)
    Spacer(modifier = Modifier.height(24.dp))
    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Field(poids, onPoids, "Poids (kg)", KeyboardType.Decimal, ImeAction.Next, Modifier.weight(1f)) { fm.moveFocus(FocusDirection.Right) }
        Field(taille, onTaille, "Taille (cm)", KeyboardType.Number, ImeAction.Done, Modifier.weight(1f)) { fm.clearFocus() }
    }
    Spacer(modifier = Modifier.height(24.dp))
    Card(
        colors = CardDefaults.cardColors(containerColor = CardBG),
        border = BorderStroke(1.dp, color.copy(alpha = 0.2f)),
        shape = RoundedCornerShape(20.dp), modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text("IMC", style = MaterialTheme.typography.labelSmall, color = TextDim)
            Text(imc ?: "—", fontSize = 48.sp, fontWeight = FontWeight.Black, color = color)
            Text(label, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun Field(value: String, onValueChange: (String) -> Unit, label: String, type: KeyboardType, action: ImeAction, modifier: Modifier = Modifier, onAction: () -> Unit) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, label = { Text(label) }, modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp), colors = fieldColors(), singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = type, imeAction = action),
        keyboardActions = KeyboardActions(onAny = { onAction() })
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    unfocusedBorderColor = Border, focusedBorderColor = MintFit,
    focusedTextColor = Color.White, unfocusedTextColor = Color.White,
    unfocusedContainerColor = CardBG, focusedContainerColor = CardBG,
    focusedLabelColor = MintFit, unfocusedLabelColor = TextDim
)