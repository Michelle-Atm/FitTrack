package com.example.fitrack.model

import org.junit.Assert.assertEquals
import org.junit.Test

class RepasModelTest {

    @Test
    fun `HeureRepas fromValeur petit-dejeuner`() {
        assertEquals(HeureRepas.PETIT_DEJEUNER, HeureRepas.fromValeur("petit-dejeuner"))
    }

    @Test
    fun `HeureRepas fromValeur dejeuner`() {
        assertEquals(HeureRepas.DEJEUNER, HeureRepas.fromValeur("dejeuner"))
    }

    @Test
    fun `HeureRepas fromValeur diner`() {
        assertEquals(HeureRepas.DINER, HeureRepas.fromValeur("diner"))
    }

    @Test
    fun `HeureRepas fromValeur collation`() {
        assertEquals(HeureRepas.COLLATION, HeureRepas.fromValeur("collation"))
    }

    @Test
    fun `HeureRepas fromValeur inconnu retourne COLLATION`() {
        assertEquals(HeureRepas.COLLATION, HeureRepas.fromValeur("snack"))
    }

    @Test
    fun `HeureRepas fromValeur chaine vide retourne COLLATION`() {
        assertEquals(HeureRepas.COLLATION, HeureRepas.fromValeur(""))
    }

    @Test
    fun `HeureRepas valeurs des enums sont correctes`() {
        assertEquals("petit-dejeuner", HeureRepas.PETIT_DEJEUNER.valeur)
        assertEquals("dejeuner", HeureRepas.DEJEUNER.valeur)
        assertEquals("diner", HeureRepas.DINER.valeur)
        assertEquals("collation", HeureRepas.COLLATION.valeur)
    }

    @Test
    fun `Repas valeurs nutritionnelles par defaut`() {
        val repas = Repas()
        assertEquals(0.0, repas.calories, 0.0)
        assertEquals(100.0, repas.quantiteG, 0.0)
        assertEquals("", repas.heure)
    }
}
