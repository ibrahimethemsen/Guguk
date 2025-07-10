package com.ibrahimethemsen.sample.data.service

import com.ibrahimethemsen.sample.data.dto.QuoteResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface QuoteService {
    @GET("random")
    suspend fun getRandomQuote(
        @Query("minLength") minLenght : Int = 150,
        @Query("maxLength") maxLength : Int = 300,
    ) : QuoteResponse
}