package com.example.fitrack.repository

import com.example.fitrack.repository.api.OFFNutriments
import com.example.fitrack.repository.api.OFFProduct
import org.junit.Assert.assertEquals
import org.junit.Test

class OFFModelesTest {

    @Test
    fun `imageUrlEffective retourne imageFrontUrl si disponible`() {
        val product = OFFProduct(
            imageFrontUrl = "https://images.openfoodfacts.org/front.jpg",
            imageUrl = "https://images.openfoodfacts.org/full.jpg"
        )
        assertEquals("https://images.openfoodfacts.org/front.jpg", product.imageUrlEffective)
    }

    @Test
    fun `imageUrlEffective fallback sur imageUrl si imageFrontUrl vide`() {
        val product = OFFProduct(
            imageFrontUrl = "",
            imageUrl = "https://images.openfoodfacts.org/full.jpg"
        )
        assertEquals("https://images.openfoodfacts.org/full.jpg", product.imageUrlEffective)
    }

    @Test
    fun `imageUrlEffective retourne chaine vide si les deux sont vides`() {
        val product = OFFProduct(imageFrontUrl = "", imageUrl = "")
        assertEquals("", product.imageUrlEffective)
    }

    @Test
    fun `OFFNutriments valeurs par defaut sont a zero`() {
        val nutriments = OFFNutriments()
        assertEquals(0.0, nutriments.calories, 0.0)
        assertEquals(0.0, nutriments.proteines, 0.0)
        assertEquals(0.0, nutriments.glucides, 0.0)
        assertEquals(0.0, nutriments.lipides, 0.0)
        assertEquals(0.0, nutriments.fibres, 0.0)
    }

    @Test
    fun `OFFProduct valeurs par defaut`() {
        val product = OFFProduct()
        assertEquals("", product.code)
        assertEquals("", product.nom)
        assertEquals("", product.imageUrlEffective)
    }
}
