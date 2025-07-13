package com.ibrahimethemsen.sample.data.service

import com.ibrahimethemsen.sample.data.dto.QuoteIdResponse
import com.ibrahimethemsen.sample.data.dto.QuoteResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface QuoteService {
    @GET("quotes/random")
    suspend fun getRandomQuote(
        @Query("limit") limit : Int = 5,
    ) : List<QuoteResponse>

    @GET("quotes/{id}")
    suspend fun getQuote(
        @Path("id") id : String
    )  : QuoteIdResponse
}