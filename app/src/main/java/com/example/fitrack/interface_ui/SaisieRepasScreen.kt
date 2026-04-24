package com.example.fitrack.interface_ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
    onRetour: () -> Unit
) {
    val rechercheState by viewModel.rechercheState.collectAsStateWithLifecycle()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var heureRepas by remember { mutableStateOf(HeureRepas.DEJEUNER) }
    var query by remember { mutableStateOf("") }
    var selectedAliment by remember { mutableStateOf<AlimentOFF?>(null) }
    var quantite by remember { mutableFloatStateOf(150f) }

    val isLoading = uiState is NutritionViewModel.NutritionUiState.Chargement

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBG)
            .imePadding()
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onRetour) {
                Icon(
                    imageVector = Icons.Filled.ArrowBack,
                    contentDescription = "Retour",
                    tint = Color.White
                )
            }
            Text(
                text = "Ajouter un repas",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                modifier = Modifier.weight(1f)
            )
        }

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Heure repas pills
            item {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
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
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(999.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MintFit,
                                selectedLabelColor = Color(0xFF002817),
                                containerColor = CardBG,
                                labelColor = TextDim
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                selected = selected, enabled = true,
                                selectedBorderColor = MintFit,
                                borderColor = Color.White.copy(alpha = 0.06f)
                            )
                        )
                    }
                }
            }

            // Search bar
            item {
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it; viewModel.rechercherAliment(it) },
                    placeholder = { Text("Rechercher un aliment...", color = TextFaint) },
                    leadingIcon = { Icon(Icons.Filled.Search, null, tint = TextDim, modifier = Modifier.size(18.dp)) },
                    trailingIcon = {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AmberFit.copy(alpha = 0.12f))
                                .border(1.dp, AmberFit.copy(alpha = 0.27f), RoundedCornerShape(10.dp))
                        ) {
                            Icon(Icons.Filled.QrCodeScanner, null, tint = AmberFit, modifier = Modifier.size(18.dp))
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedBorderColor = Color.White.copy(alpha = 0.06f),
                        focusedBorderColor = MintFit,
                        unfocusedTextColor = Color.White,
                        focusedTextColor = Color.White,
                        unfocusedContainerColor = CardBG,
                        focusedContainerColor = CardBG
                    ),
                    singleLine = true
                )
            }

            // Loading
            if (rechercheState is NutritionViewModel.RechercheState.Chargement) {
                item {
                    LinearProgressIndicator(
                        modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(999.dp)),
                        color = MintFit,
                        trackColor = Color.White.copy(alpha = 0.06f)
                    )
                }
            }

            // Results
            val resultats = (rechercheState as? NutritionViewModel.RechercheState.Resultats)?.aliments ?: emptyList()
            if (resultats.isNotEmpty()) {
                item {
                    Text(
                        text = "${resultats.size} résultat${if (resultats.size > 1) "s" else ""}",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextDim
                    )
                }
                items(resultats, key = { it.code }) { aliment ->
                    val selected = aliment.code == selectedAliment?.code
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardBG)
                            .border(
                                width = if (selected) 1.5.dp else 1.dp,
                                color = if (selected) MintFit else Color.White.copy(alpha = 0.06f),
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        androidx.compose.material3.TextButton(
                            onClick = { selectedAliment = if (selected) null else aliment },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = aliment.nom,
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "P ${aliment.proteines.roundToInt()} · G ${aliment.glucides.roundToInt()} · L ${aliment.lipides.roundToInt()}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextDim
                                    )
                                }
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = "${aliment.calories.roundToInt()}",
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            fontWeight = FontWeight.ExtraBold,
                                            fontSize = 15.sp
                                        ),
                                        color = Color.White
                                    )
                                    Text(
                                        text = "kcal/100g",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                        color = TextFaint
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Error
            (rechercheState as? NutritionViewModel.RechercheState.Erreur)?.let { err ->
                item {
                    Text(
                        text = err.message,
                        color = Color.Red,
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Quantity panel
            selectedAliment?.let { aliment ->
                item {
                    val cal = (aliment.calories * quantite / 100).roundToInt()
                    val prot = (aliment.proteines * quantite / 100 * 10).roundToInt() / 10.0
                    val gluc = (aliment.glucides * quantite / 100 * 10).roundToInt() / 10.0
                    val lip = (aliment.lipides * quantite / 100 * 10).roundToInt() / 10.0

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(CardBG2)
                            .border(1.dp, MintFit.copy(alpha = 0.27f), RoundedCornerShape(12.dp))
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = aliment.nom,
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("QUANTITÉ", style = MaterialTheme.typography.labelSmall, color = TextDim)
                            Text(
                                text = "${quantite.roundToInt()} g",
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontWeight = FontWeight.ExtraBold,
                                    fontSize = 18.sp,
                                    letterSpacing = (-0.4).sp
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
                                inactiveTrackColor = Color.White.copy(alpha = 0.06f)
                            )
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            MacroCellSaisie("kcal", "$cal", MintFit, Modifier.weight(1.3f))
                            MacroCellSaisie("Prot", "${prot}g", MintFit, Modifier.weight(1f))
                            MacroCellSaisie("Gluc", "${gluc}g", VioletFit, Modifier.weight(1f))
                            MacroCellSaisie("Lip", "${lip}g", AmberFit, Modifier.weight(1f))
                        }
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(16.dp)) }
        }

        // Bouton Ajouter
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkBG)
                .padding(horizontal = 20.dp, vertical = 12.dp)
                .padding(bottom = 12.dp)
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
                modifier = Modifier.fillMaxWidth().height(54.dp),
                enabled = selectedAliment != null && !isLoading,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintFit,
                    disabledContainerColor = MintFit.copy(alpha = 0.45f)
                ),
                shape = RoundedCornerShape(12.dp)
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color(0xFF002817), modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                } else {
                    Text("Ajouter", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF002817))
                }
            }
        }
    }
}

@Composable
private fun MacroCellSaisie(label: String, value: String, color: Color, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(Color.White.copy(alpha = 0.03f))
            .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(10.dp))
            .padding(vertical = 10.dp, horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp, fontWeight = FontWeight.ExtraBold),
            color = TextFaint
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.ExtraBold,
                fontSize = 16.sp,
                letterSpacing = (-0.3).sp
            ),
            color = color
        )
    }
}
