# FitTrack

Application Android de suivi sportif et nutritionnel en Kotlin, inspirée de Duolingo : avatars évolutifs, side quests dynamiques et scoring basé sur l'effort relatif.

## Architecture

**MVVM** avec Jetpack Compose, Firebase et Retrofit.

```
model/          — Entités (User, Repas, Objectif, SideQuest, Seance, AlimentOFF)
repository/     — Interfaces (AuthRepository, NutritionRepository, ObjectifRepository)
  firestore/    — Implémentations Firebase Firestore
  api/          — Service Retrofit Open Food Facts + modèles
viewmodel/      — AuthViewModel, NutritionViewModel, ObjectifViewModel
interface_ui/   — Composables Jetpack Compose
```

## Fonctionnalités implémentées (branche develop)

- Authentification Firebase (inscription, connexion, déconnexion)
- Calcul IMC et génération de programme personnalisé selon objectif + expérience
- Suivi nutritionnel journalier (repas, totaux calories/protéines/glucides/lipides/fibres)
- Recherche d'aliments via Open Food Facts API (texte + code-barres)
- Suivi des objectifs journaliers avec progression
- Système de séances et side quests avec XP

## Stack technique

| Composant | Librairie | Version |
|-----------|-----------|---------|
| UI | Jetpack Compose | BOM 2024.09.00 |
| Auth + DB | Firebase Auth + Firestore | BOM 33.7.0 |
| API REST | Retrofit + OkHttp | 2.11.0 / 4.12.0 |
| Async | Kotlin Coroutines | 1.9.0 |
| ViewModel | Lifecycle ViewModel | 2.10.0 |

## Configuration

1. Cloner le repo
2. Ouvrir dans Android Studio
3. Placer le fichier `google-services.json` dans `app/` (fourni séparément)
4. `./gradlew build`
5. Lancer sur émulateur API 24+ ou device physique

## API externe

[Open Food Facts](https://world.openfoodfacts.org/) — données nutritionnelles libres.
- Recherche : `GET /cgi/search.pl?search_terms=...`
- Code-barres : `GET /product/{barcode}.json`
