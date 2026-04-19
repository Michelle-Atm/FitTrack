package com.example.fitrack.repository.api

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface OpenFoodFactsApiService {

    @GET("product/{barcode}.json")
    suspend fun produitParCodeBarres(
        @Path("barcode") barcode: String
    ): Response<OFFProductResponse>

    @GET("cgi/search.pl")
    suspend fun rechercherAliments(
        @Query("search_terms") terme: String,
        @Query("json") json: Int = 1,
        @Query("page_size") taille: Int = 20,
        @Query("fields") champs: String = "code,product_name,nutriments,image_url"
    ): Response<OFFSearchResponse>
}
