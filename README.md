# FitTrack

Application Android de suivi sportif et nutritionnel, développée en Kotlin avec Jetpack Compose. FitTrack adopte une approche inspirée de la gamification (avatars évolutifs, système XP, side quests, classement social) pour encourager l'engagement sportif quotidien.

---

## Fonctionnalités

### Authentification & Profil
- Inscription en 3 étapes : identifiants → objectif fitness → données corporelles
- Génération automatique d'un pseudo à partir du prénom (ex. `Pierre247`) avec bouton de régénération
- Connexion Firebase Authentication avec restauration silencieuse de session
- Calcul automatique de l'IMC et génération d'un programme d'entraînement personnalisé selon l'objectif (perte de poids, prise de masse, endurance, maintien) et le niveau d'expérience
- Modification du profil (poids, taille, objectif, allergies) avec recalcul IMC en temps réel
- Blocage de connexion pour les comptes suspendus

### Suivi nutritionnel
- Journal alimentaire journalier avec totaux calories / protéines / glucides / lipides / fibres
- Recherche d'aliments via l'API Open Food Facts (texte libre)
- Scan de code-barres produit via la caméra
- Historique nutritionnel navigable par jour
- Alertes allergènes personnalisées à la saisie

### Objectifs journaliers
- Suivi des objectifs calories, pas, séances avec barres de progression
- Logging de séances sportives (8 types d'activités, durée réglable par slider)
- Calcul des calories dépensées par la formule MET × poids × durée
- Historique des séances récentes avec re-logging en un tap
- Side quests déblocables selon le niveau XP et le streak de l'utilisateur
- Overlay de célébration à l'atteinte des objectifs du jour
- Score journalier pondéré (calories 40 % · pas 30 % · séances 30 %)

### Podomètre
- Comptage de pas via le capteur matériel `TYPE_STEP_COUNTER`
- Gestion de la permission `ACTIVITY_RECOGNITION` (Android 10+)
- Estimation des calories dépensées et de la distance parcourue
- Timer de session actif/inactif avec persistance dans le ViewModel

### GPS & Trajet
- Enregistrement de trajet GPS en temps réel avec carte interactive (Maps Compose)
- Calcul de la distance parcourue et de la vitesse moyenne
- Sauvegarde du trajet dans Firestore à l'arrêt de la session

### Avatar & Progression
- Choix d'espèce d'avatar (renard, pingouin, panda, axolotl)
- Système XP : 300 XP par niveau, barre de progression dans le profil
- Ajout d'XP à chaque séance enregistrée avec détection de montée de niveau
- Affichage de l'état visuel de l'avatar selon le niveau atteint

### Classement social
- Leaderboard hebdomadaire avec podium (top 3) et liste complète
- Filtrage par catégorie d'expérience (débutant / intermédiaire / avancé)
- Consultation du profil public d'un autre utilisateur

### Administration
- Panneau admin protégé par route guard (`isAdmin` posé manuellement dans Firestore)
- Onglet Utilisateurs : liste, suspension/rétablissement, suppression avec confirmation
- Onglet Paliers : configuration des seuils de score par niveau (éditables en temps réel)
- Onglet Statistiques : nombre d'utilisateurs, séances totales, score XP moyen

---

## Architecture

L'application suit le pattern **MVVM** (Model – View – ViewModel) recommandé par Google pour Android, combiné au **Repository pattern** pour l'isolation des sources de données.

```
com.example.fitrack/
├── model/              Entités de données (User, Repas, Objectif, Seance, SideQuest…)
├── repository/         Interfaces des repositories
│   ├── firestore/      Implémentations Firebase Firestore
│   └── api/            Service Retrofit Open Food Facts
├── viewmodel/          ViewModels (AuthViewModel, NutritionViewModel, ObjectifViewModel…)
├── interface_ui/       Composables Jetpack Compose (un fichier par écran)
├── components/         Composants réutilisables (BottomNavBar, ProgressRing, SideQuestCard…)
├── navigation/         NavGraph, routes, Scaffold global
└── ui/theme/           Couleurs, typographie, thème Material3
```

Chaque ViewModel expose son état via des `StateFlow` et ses événements ponctuels (snackbar, célébration) via des `SharedFlow(extraBufferCapacity = 1)`. Les couches sont découplées par injection de dépendance manuelle (interfaces + constructeur), ce qui rend chaque composant testable indépendamment.

---

## Stack technique

| Composant | Technologie | Version |
|---|---|---|
| Langage | Kotlin | 2.0 |
| UI | Jetpack Compose + Material3 | BOM 2024.09.00 |
| Navigation | Navigation Compose | 2.8.x |
| Authentification | Firebase Authentication | BOM 33.7.0 |
| Base de données | Cloud Firestore | BOM 33.7.0 |
| API nutritionnelle | Retrofit + OkHttp → Open Food Facts | 2.11.0 / 4.12.0 |
| Carte GPS | Maps Compose (Google Maps) | — |
| Capteur pas | Android `TYPE_STEP_COUNTER` + Accompanist Permissions | — |
| Asynchrone | Kotlin Coroutines + Flow | 1.9.0 |
| ViewModel | Lifecycle ViewModel | 2.10.0 |
| Tests unitaires | JUnit 4 + Kotlinx Coroutines Test | — |
| Tests UI | Jetpack Compose Test (instrumented) | — |

---

## Configuration et lancement

### Prérequis
- Android Studio Hedgehog ou supérieur
- JDK 17
- Un appareil ou émulateur Android API 24 minimum (API 29+ recommandé pour le podomètre)

### Installation

```bash
# 1. Cloner le dépôt
git clone <url-du-repo>
cd FitTrack

# 2. Placer le fichier de configuration Firebase dans app/
# Le fichier google-services.json est fourni séparément (non versionné)
cp /chemin/vers/google-services.json app/

# 3. Compiler
./gradlew assembleDebug
```

### Lancer l'application

Ouvrir le projet dans Android Studio, sélectionner un émulateur ou un appareil connecté, et cliquer sur **Run**. La branche principale de développement est `develop`.

### Configurer un compte administrateur

Le rôle admin n'est pas assignable depuis l'application. Pour promouvoir un utilisateur :
1. Ouvrir la [console Firebase](https://console.firebase.google.com)
2. Naviguer vers **Firestore → Collection `users` → Document `{uid}`**
3. Passer le champ `isAdmin` à `true`

---

## Tests

### Tests unitaires

```bash
./gradlew testDebugUnitTest
```

Couvrent les ViewModels suivants avec des fakes injectés (aucune dépendance Firebase) :

| ViewModel | Cas couverts |
|---|---|
| `AuthViewModel` | IMC, catégories, génération programme, connexion, inscription, suspension |
| `ObjectifViewModel` | Progression, score hebdo, conditions side quests, logging séance |
| `AvatarViewModel` | Chargement avatar, ajout XP, montée de niveau, événements |
| `SensorViewModel` | Baseline pas, start/stop, timer, cas capteur indisponible |
| `AdminViewModel` | Chargement utilisateurs/paliers/stats, suppression, suspension, événements |

### Tests instrumentés (UI)

```bash
./gradlew connectedDebugAndroidTest
```

Tests Compose sur 8 écrans : Login, Inscription, Home, Profil, ObjectifsScreen, Avatar, Leaderboard, ProfilPublic.

---

## API externe

**Open Food Facts** — base de données nutritionnelle ouverte et collaborative.

| Endpoint | Usage |
|---|---|
| `GET /cgi/search.pl?search_terms={q}&json=1` | Recherche par nom |
| `GET /api/v0/product/{barcode}.json` | Recherche par code-barres |

Aucune clé API requise. Les données sont sous licence ODbL.

---

## Points d'attention pour le déploiement

- Le fichier `google-services.json` ne doit **jamais** être versionné (il est dans `.gitignore`).
- Les requêtes Firestore combinant filtre d'égalité et tri (`userId + orderBy date`) nécessitent des **index composites** à créer dans la console Firebase (Firestore → Index).
- La permission `ACTIVITY_RECOGNITION` est demandée à l'exécution sur Android 10+. Sur émulateur, le capteur de pas peut être absent — l'écran podomètre affiche alors un message d'indisponibilité.
