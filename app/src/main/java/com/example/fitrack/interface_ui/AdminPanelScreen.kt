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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Save
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults.SecondaryIndicator
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.fitrack.model.ScorePalier
import com.example.fitrack.model.User
import com.example.fitrack.repository.StatsGlobales
import com.example.fitrack.ui.theme.AmberFit
import com.example.fitrack.ui.theme.CardBG
import com.example.fitrack.ui.theme.DangerFit
import com.example.fitrack.ui.theme.DarkBG
import com.example.fitrack.ui.theme.MintFit
import com.example.fitrack.ui.theme.TextDim
import com.example.fitrack.ui.theme.VioletFit
import com.example.fitrack.viewmodel.AdminViewModel

@Composable
fun AdminPanelScreen(
    adminViewModel: AdminViewModel,
    onRetour: () -> Unit
) {
    val utilisateursState by adminViewModel.utilisateursState.collectAsStateWithLifecycle()
    val paliersState      by adminViewModel.paliersState.collectAsStateWithLifecycle()
    val statsState        by adminViewModel.statsState.collectAsStateWithLifecycle()

    var tabIndex by remember { mutableIntStateOf(0) }
    val tabs = listOf("Utilisateurs", "Paliers", "Stats")

    val snackbarHostState = remember { SnackbarHostState() }
    var utilisateurASupprimer by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        adminViewModel.chargerTousLesUtilisateurs()
        adminViewModel.chargerPaliers()
        adminViewModel.chargerStats()
    }
    LaunchedEffect(snackbarHostState) {
        adminViewModel.actionEvenement.collect { message ->
            snackbarHostState.showSnackbar(message, duration = SnackbarDuration.Short)
        }
    }

    utilisateurASupprimer?.let { cible ->
        AlertDialog(
            onDismissRequest = { utilisateurASupprimer = null },
            title = { Text("Supprimer l'utilisateur ?", color = Color.White) },
            text = {
                Text(
                    "Toutes les données de « ${cible.nom.ifBlank { cible.email }} » seront effacées.",
                    color = TextDim
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    adminViewModel.supprimerUtilisateur(cible.uid)
                    utilisateurASupprimer = null
                }) { Text("Supprimer", color = DangerFit, fontWeight = FontWeight.Bold) }
            },
            dismissButton = {
                TextButton(onClick = { utilisateurASupprimer = null }) {
                    Text("Annuler", color = Color.White)
                }
            },
            containerColor = CardBG
        )
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = DarkBG
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onRetour) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, "Retour", tint = Color.White)
                }
                Spacer(Modifier.width(8.dp))
                Icon(
                    Icons.Filled.AdminPanelSettings,
                    contentDescription = null,
                    tint = VioletFit,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    "Panneau Admin",
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = Color.White
                )
            }

            TabRow(
                selectedTabIndex = tabIndex,
                containerColor = CardBG,
                contentColor = VioletFit,
                indicator = { tabPositions ->
                    SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[tabIndex]),
                        color = VioletFit
                    )
                }
            ) {
                tabs.forEachIndexed { i, titre ->
                    Tab(
                        selected = tabIndex == i,
                        onClick = { tabIndex = i },
                        text = {
                            Text(
                                titre,
                                fontWeight = if (tabIndex == i) FontWeight.ExtraBold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        selectedContentColor = VioletFit,
                        unselectedContentColor = TextDim
                    )
                }
            }

            when (tabIndex) {
                0 -> OngletUtilisateurs(
                    state = utilisateursState,
                    onSupprimer = { utilisateurASupprimer = it },
                    onSuspendre = { uid, s -> adminViewModel.suspendreUtilisateur(uid, s) }
                )
                1 -> OngletPaliers(
                    state = paliersState,
                    onMettreAJour = { adminViewModel.mettreAJourPalier(it) }
                )
                2 -> OngletStats(state = statsState)
            }
        }
    }
}

@Composable
private fun OngletUtilisateurs(
    state: AdminViewModel.UtilisateursState,
    onSupprimer: (User) -> Unit,
    onSuspendre: (String, Boolean) -> Unit
) {
    when (state) {
        is AdminViewModel.UtilisateursState.Chargement -> CentreChargement()
        is AdminViewModel.UtilisateursState.Erreur ->
            CentreErreur(state.message)
        is AdminViewModel.UtilisateursState.Succes -> {
            if (state.utilisateurs.isEmpty()) {
                CentreVide("Aucun utilisateur trouvé")
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {
                        Text(
                            "${state.utilisateurs.size} utilisateur(s)",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextDim,
                            modifier = Modifier.padding(bottom = 4.dp)
                        )
                    }
                    items(state.utilisateurs, key = { it.uid }) { user ->
                        CarteUtilisateur(
                            user = user,
                            onSupprimer = { onSupprimer(user) },
                            onSuspendre = { onSuspendre(user.uid, !user.suspendu) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CarteUtilisateur(
    user: User,
    onSupprimer: () -> Unit,
    onSuspendre: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBG),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(VioletFit.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = user.nom.firstOrNull()?.uppercase() ?: user.email.firstOrNull()?.uppercase() ?: "?",
                    color = VioletFit,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 16.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = user.nom.ifBlank { "—" },
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    if (user.isAdmin) {
                        BadgeAdmin()
                    }
                    if (user.suspendu) {
                        BadgeSuspendu()
                    }
                }
                Text(user.email, style = MaterialTheme.typography.labelSmall, color = TextDim)
                Text(
                    "Niv. ${user.niveau} · ${user.xp} XP",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextDim.copy(alpha = 0.6f)
                )
            }

            IconButton(onClick = onSuspendre, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = if (user.suspendu) Icons.Filled.LockOpen else Icons.Filled.Block,
                    contentDescription = if (user.suspendu) "Lever la suspension" else "Suspendre",
                    tint = if (user.suspendu) MintFit else AmberFit,
                    modifier = Modifier.size(18.dp)
                )
            }

            if (!user.isAdmin) {
                IconButton(onClick = onSupprimer, modifier = Modifier.size(36.dp)) {
                    Icon(
                        Icons.Filled.Delete,
                        contentDescription = "Supprimer",
                        tint = DangerFit,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun BadgeAdmin() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(VioletFit.copy(alpha = 0.2f))
            .border(1.dp, VioletFit.copy(alpha = 0.5f), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text("ADMIN", fontSize = 8.sp, color = VioletFit, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun BadgeSuspendu() {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(DangerFit.copy(alpha = 0.15f))
            .border(1.dp, DangerFit.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
            .padding(horizontal = 4.dp, vertical = 1.dp)
    ) {
        Text("SUSPENDU", fontSize = 8.sp, color = DangerFit, fontWeight = FontWeight.ExtraBold)
    }
}

@Composable
private fun OngletPaliers(
    state: AdminViewModel.PaliersState,
    onMettreAJour: (ScorePalier) -> Unit
) {
    when (state) {
        is AdminViewModel.PaliersState.Chargement -> CentreChargement()
        is AdminViewModel.PaliersState.Erreur     -> CentreErreur(state.message)
        is AdminViewModel.PaliersState.Succes     -> {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Modifiez les seuils de score par niveau.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextDim,
                        modifier = Modifier.padding(bottom = 4.dp)
                    )
                }
                items(state.paliers, key = { it.id }) { palier ->
                    CartePalier(palier = palier, onSauvegarder = onMettreAJour)
                }
            }
        }
    }
}

@Composable
private fun CartePalier(palier: ScorePalier, onSauvegarder: (ScorePalier) -> Unit) {
    var label    by remember(palier.id) { mutableStateOf(palier.label) }
    var seuilMin by remember(palier.id) { mutableStateOf(palier.seuilMin.toString()) }
    var seuilMax by remember(palier.id) {
        mutableStateOf(if (palier.seuilMax == Int.MAX_VALUE) "∞" else palier.seuilMax.toString())
    }
    val modifie = label != palier.label
        || seuilMin != palier.seuilMin.toString()
        || (palier.seuilMax != Int.MAX_VALUE && seuilMax != palier.seuilMax.toString())

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = CardBG),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "NIV. ${palier.niveau}",
                    style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.ExtraBold),
                    color = VioletFit
                )
                if (modifie) {
                    IconButton(
                        onClick = {
                            onSauvegarder(
                                palier.copy(
                                    label    = label,
                                    seuilMin = seuilMin.toIntOrNull() ?: palier.seuilMin,
                                    seuilMax = seuilMax.toIntOrNull() ?: palier.seuilMax
                                )
                            )
                        },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Filled.Save, "Sauvegarder", tint = MintFit, modifier = Modifier.size(18.dp))
                    }
                } else {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MintFit.copy(alpha = 0.4f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            PalierField("Label", label, onValueChange = { label = it })
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                PalierField("Seuil min", seuilMin, onValueChange = { seuilMin = it }, modifier = Modifier.weight(1f))
                PalierField("Seuil max", seuilMax, onValueChange = { seuilMax = it }, modifier = Modifier.weight(1f), enabled = palier.seuilMax != Int.MAX_VALUE)
            }
        }
    }
}

@Composable
private fun PalierField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth(),
    enabled: Boolean = true
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, fontSize = 11.sp) },
        modifier = modifier.height(56.dp),
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(keyboardType = if (label.contains("Seuil")) KeyboardType.Number else KeyboardType.Text),
        shape = RoundedCornerShape(8.dp),
        colors = OutlinedTextFieldDefaults.colors(
            unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
            focusedBorderColor = VioletFit,
            unfocusedTextColor = Color.White,
            focusedTextColor = Color.White,
            unfocusedContainerColor = DarkBG,
            focusedContainerColor = DarkBG,
            disabledTextColor = TextDim,
            disabledBorderColor = Color.White.copy(alpha = 0.05f),
            disabledContainerColor = DarkBG
        )
    )
}

@Composable
private fun OngletStats(state: AdminViewModel.StatsState) {
    when (state) {
        is AdminViewModel.StatsState.Chargement -> CentreChargement()
        is AdminViewModel.StatsState.Erreur     -> CentreErreur(state.message)
        is AdminViewModel.StatsState.Succes     -> OngletStatsContenu(state.stats)
    }
}

@Composable
private fun OngletStatsContenu(stats: StatsGlobales) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Métriques globales", style = MaterialTheme.typography.labelSmall, color = TextDim)
        CarteStatGlobale(
            icon = Icons.Filled.Group,
            label = "Utilisateurs inscrits",
            valeur = stats.totalUtilisateurs.toString(),
            couleur = MintFit
        )
        CarteStatGlobale(
            icon = Icons.Filled.AdminPanelSettings,
            label = "Séances enregistrées",
            valeur = stats.totalSeances.toString(),
            couleur = AmberFit
        )
        CarteStatGlobale(
            icon = Icons.Filled.CheckCircle,
            label = "Score XP moyen",
            valeur = "%.0f pts".format(stats.scoreMoyen),
            couleur = VioletFit
        )
    }
}

@Composable
private fun CarteStatGlobale(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    valeur: String,
    couleur: Color
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = couleur.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Icon(icon, contentDescription = null, tint = couleur, modifier = Modifier.size(24.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(label, style = MaterialTheme.typography.bodySmall, color = TextDim)
                Text(
                    valeur,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.ExtraBold, fontSize = 22.sp
                    ),
                    color = couleur
                )
            }
        }
    }
}

@Composable
private fun CentreChargement() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator(color = VioletFit)
    }
}

@Composable
private fun CentreErreur(message: String) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(message, color = DangerFit, style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun CentreVide(message: String) {
    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
        Text(message, color = TextDim, style = MaterialTheme.typography.bodyMedium)
    }
}
