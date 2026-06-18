package com.example.fitrack.utils

fun emojiAnimal(espece: String): String = when (espece) {
    "renard"   -> "🦊"
    "pingouin" -> "🐧"
    "panda"    -> "🐼"
    "axolotl"  -> "🦎"
    else       -> "🐾"
}

fun etatEmoji(etat: String): String = when (etat) {
    "champion" -> "🏆"
    "heureux"  -> "😊"
    "neutre"   -> "😐"
    "triste"   -> "😢"
    else       -> "🐾"
}
