package com.example.fitrack.interface_ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.fitrack.model.AlimentOFF
import com.example.fitrack.model.HeureRepas
import com.example.fitrack.model.Repas
import com.example.fitrack.ui.theme.AmberFit
import com.example.fitrack.ui.theme.CardBG
import com.example.fitrack.ui.theme.CardBG2
import com.example.fitrack.ui.theme.DangerFit
import com.example.fitrack.ui.theme.DarkBG
import com.example.fitrack.ui.theme.MintFit
import com.example.fitrack.ui.theme.TextDim
import com.example.fitrack.ui.theme.TextFaint
import com.example.fitrack.ui.theme.VioletFit
import com.example.fitrack.viewmodel.NutritionViewModel
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaisieRepasScreen(
    viewModel: NutritionViewModel,
    userId: String,
    onRetour: () -> Unit,
    allergiesUtilisateur: List<String> = emptyList()
) {
    val rechercheState by viewModel.rechercheState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var heureRepas by remember { mutableStateOf(HeureRepas.DEJEUNER) }
    var query by remember { mutableStateOf("") }
    var selectedAliment by remember { mutableStateOf<AlimentOFF?>(null) }
    var quantite by remember { mutableFloatStateOf(150f) }

    val isLoading = uiState is NutritionViewModel.NutritionUiState.Chargement

    Scaffold(
        containerColor = DarkBG,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkBG)
                .padding(innerPadding)
                .imePadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onRetour) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Retour",
                        tint = Color.White
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Ajouter un repas",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = Color.White,
                    modifier = Modifier.weight(1f)
                )
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                item {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                    ) {
                        HeureRepas.entries.forEach { h ->
                            val selected = h == heureRepas
                            FilterChip(
                                selected = selected,
                                onClick = { heureRepas = h },
                                label = {
                                    Text(
                                        text = when (h) {
                                            HeureRepas.PETIT_DEJEUNER -> "Petit-déj"
                                            HeureRepas.DEJEUNER -> "Déjeuner"
                                            HeureRepas.DINER -> "Dîner"
                                            HeureRepas.COLLATION -> "Collation"
                                        },
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 12.sp
                                    )
                                },
                                shape = RoundedCornerShape(14.dp),
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MintFit,
                                    selectedLabelColor = Color(0xFF002817),
                                    containerColor = CardBG,
                                    labelColor = TextDim
                                ),
                                border = FilterChipDefaults.filterChipBorder(
                                    selected = selected, enabled = true,
                                    selectedBorderColor = MintFit,
                                    borderColor = Color.White.copy(alpha = 0.05f)
                                )
                            )
                        }
                    }
                }

                item {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it; viewModel.rechercherAliment(it) },
                        placeholder = { Text("Rechercher un aliment...", color = TextFaint) },
                        leadingIcon = { Icon(Icons.Filled.Search, null, tint = TextDim, modifier = Modifier.size(20.dp)) },
                        trailingIcon = {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(40.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(AmberFit.copy(alpha = 0.15f))
                                    .border(1.dp, AmberFit.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                            ) {
                                Icon(Icons.Filled.QrCodeScanner, null, tint = AmberFit, modifier = Modifier.size(20.dp))
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            unfocusedBorderColor = Color.White.copy(alpha = 0.05f),
                            focusedBorderColor = MintFit,
                            unfocusedTextColor = Color.White,
                            focusedTextColor = Color.White,
                            unfocusedContainerColor = CardBG,
                            focusedContainerColor = CardBG,
                            focusedLabelColor = MintFit,
                            unfocusedLabelColor = TextDim
                        ),
                        singleLine = true
                    )
                }

                if (rechercheState is NutritionViewModel.RechercheState.Chargement) {
                    item {
                        LinearProgressIndicator(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(99.dp)),
                            color = MintFit,
                            trackColor = Color.White.copy(alpha = 0.05f)
                        )
                    }
                }

                val resultats = (rechercheState as? NutritionViewModel.RechercheState.Resultats)?.aliments ?: emptyList()
                if (resultats.isNotEmpty()) {
                    item {
                        Text(
                            text = "${resultats.size} résultat${if (resultats.size > 1) "s" else ""}",
                            style = MaterialTheme.typography.labelSmall.copy(
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            ),
                            color = TextDim
                        )
                    }
                    items(resultats, key = { it.code }) { aliment ->
                        val selected = aliment.code == selectedAliment?.code
                        val allergenesTrouves = aliment.allergenes
                            .filter { tag ->
                                allergiesUtilisateur.any { allergie ->
                                    tag.contains(allergie.lowercase())
                                }
                            }
                            .map { it.removePrefix("en:") }
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(CardBG)
                                .border(
                                    BorderStroke(
                                        width = if (selected) 1.5.dp else 1.dp,
                                        color = if (selected) MintFit else Color.White.copy(alpha = 0.04f)
                                    ),
                                    shape = RoundedCornerShape(16.dp)
                                )
                        ) {
                            androidx.compose.material3.TextButton(
                                onClick = { selectedAliment = if (selected) null else aliment },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(16.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = aliment.nom,
                                            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                        )
                                        Spacer(modifier = Modifier.height(4.dp))
                                        Text(
                                            text = "P ${aliment.proteines.roundToInt()} · G ${aliment.glucides.roundToInt()} · L ${aliment.lipides.roundToInt()}",
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Medium),
                                            color = TextDim
                                        )
                                    }
                                    Column(horizontalAlignment = Alignment.End) {
                                        Text(
                                            text = "${aliment.calories.roundToInt()}",
                                            style = MaterialTheme.typography.titleLarge.copy(
                                                fontWeight = FontWeight.Black,
                                                letterSpacing = (-0.5).sp
                                            ),
                                            color = Color.White
                                        )
                                        Text(
                                            text = "kcal/100g",
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                            color = TextFaint
                                        )
                                    }
                                }
                            }
                            if (allergenesTrouves.isNotEmpty()) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(bottomStart = 16.dp, bottomEnd = 16.dp))
                                        .background(DangerFit.copy(alpha = 0.12f))
                                        .padding(horizontal = 16.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Warning,
                                        contentDescription = null,
                                        tint = DangerFit,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = "Contient : ${allergenesTrouves.joinToString(", ")}",
                                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                                        color = DangerFit
                                    )
                                }
                            }
                        }
                    }
                }

                (rechercheState as? NutritionViewModel.RechercheState.Vide)?.let { vide ->
                    item {
                        Text(
                            text = vide.message,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                            color = TextDim,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )
                    }
                }

                (rechercheState as? NutritionViewModel.RechercheState.Erreur)?.let { err ->
                    item {
                        Text(
                            text = err.message,
                            color = DangerFit,
                            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                selectedAliment?.let { aliment ->
                    item {
                        val cal = (aliment.calories * quantite / 100).roundToInt()
                        val prot = (aliment.proteines * quantite / 100 * 10).roundToInt() / 10.0
                        val gluc = (aliment.glucides * quantite / 100 * 10).roundToInt() / 10.0
                        val lip = (aliment.lipides * quantite / 100 * 10).roundToInt() / 10.0

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(24.dp))
                                .background(CardBG2)
                                .border(BorderStroke(1.dp, MintFit.copy(alpha = 0.2f)), RoundedCornerShape(24.dp))
                                .padding(20.dp),
                            verticalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Text(
                                text = aliment.nom,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "QUANTITÉ",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        letterSpacing = 1.sp
                                    ),
                                    color = TextDim
                                )
                                Text(
                                    text = "${quantite.roundToInt()} g",
                                    style = MaterialTheme.typography.titleLarge.copy(
                                        fontWeight = FontWeight.Black,
                                        letterSpacing = (-0.5).sp
                                    ),
                                    color = MintFit
                                )
                            }
                            Slider(
                                value = quantite,
                                onValueChange = { quantite = it },
                                valueRange = 10f..500f,
                                steps = 0,
                                colors = SliderDefaults.colors(
                                    thumbColor = MintFit,
                                    activeTrackColor = MintFit,
                                    inactiveTrackColor = Color.White.copy(alpha = 0.05f)
                                )
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                MacroCellSaisie("kcal", "$cal", MintFit, Modifier.weight(1.2f))
                                MacroCellSaisie("Prot", "${prot}g", MintFit, Modifier.weight(1f))
                                MacroCellSaisie("Gluc", "${gluc}g", VioletFit, Modifier.weight(1f))
                                MacroCellSaisie("Lip", "${lip}g", AmberFit, Modifier.weight(1f))
                            }
                        }
                    }
                }

                item { Spacer(modifier = Modifier.height(8.dp)) }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBG)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Button(
                    onClick = {
                        val aliment = selectedAliment ?: return@Button
                        val repas = Repas(
                            nom = aliment.nom,
                            heure = heureRepas.valeur,
                            date = System.currentTimeMillis(),
                            calories = aliment.calories * quantite / 100,
                            proteines = aliment.proteines * quantite / 100,
                            glucides = aliment.glucides * quantite / 100,
                            lipides = aliment.lipides * quantite / 100,
                            fibres = aliment.fibres * quantite / 100,
                            quantiteG = quantite.toDouble()
                        )
                        viewModel.ajouterRepas(repas, userId)
                        onRetour()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    enabled = selectedAliment != null && !isLoading,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MintFit,
                        disabledContainerColor = MintFit.copy(alpha = 0.25f)
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(color = Color(0xFF002817), modifier = Modifier.size(24.dp), strokeWidth = 2.5.dp)
                    } else {
                        Text("Ajouter", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF002817))
                    }
                }
            }
        }
    }
}

@Composable
private fun MacroCellSaisie(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(Color.White.copy(alpha = 0.02f))
            .border(BorderStroke(1.dp, Color.White.copy(alpha = 0.04f)), RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp, horizontal = 6.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.Black, letterSpacing = 0.5.sp),
            color = TextFaint
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                letterSpacing = (-0.3).sp
            ),
            color = color
        )
    }
}