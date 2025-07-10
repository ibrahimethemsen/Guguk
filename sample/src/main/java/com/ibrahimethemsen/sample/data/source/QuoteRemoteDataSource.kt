package com.ibrahimethemsen.sample.data.source

import com.ibrahimethemsen.sample.data.dto.QuoteResponse

interface QuoteRemoteDataSource {
    suspend fun getRandomQuote(minLength: Int, maxLength: Int) : QuoteResponse
}