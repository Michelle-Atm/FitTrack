package com.example.fitrack.repository.api

import com.google.gson.annotations.SerializedName

data class OFFProductResponse(
    @SerializedName("status") val status: Int = 0,
    @SerializedName("product") val product: OFFProduct? = null
)

data class OFFSearchResponse(
    @SerializedName("count") val count: Int = 0,
    @SerializedName("products") val products: List<OFFProduct> = emptyList()
)

data class OFFProduct(
    @SerializedName("code") val code: String = "",
    @SerializedName("product_name") val nom: String = "",
    @SerializedName("nutriments") val nutriments: OFFNutriments? = null,
    @SerializedName("image_front_url") val imageFrontUrl: String = "",
    @SerializedName("image_url") val imageUrl: String = "",
    @SerializedName("allergens_tags") val allergenes: List<String>? = null
) {
    val imageUrlEffective: String
        get() = imageFrontUrl.takeIf { it.isNotBlank() }
            ?: imageUrl.takeIf { it.isNotBlank() }
            ?: ""
}

data class OFFNutriments(
    @SerializedName("energy-kcal_100g") val calories: Double? = 0.0,
    @SerializedName("proteins_100g") val proteines: Double? = 0.0,
    @SerializedName("carbohydrates_100g") val glucides: Double? = 0.0,
    @SerializedName("fat_100g") val lipides: Double? = 0.0,
    @SerializedName("fiber_100g") val fibres: Double? = 0.0
)
