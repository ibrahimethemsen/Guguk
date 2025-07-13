package com.ibrahimethemsen.sample.data.source

import com.ibrahimethemsen.sample.data.dto.QuoteIdResponse
import com.ibrahimethemsen.sample.data.dto.QuoteResponse

interface QuoteRemoteDataSource {
    suspend fun getRandomQuote(limit : Int) : List<QuoteResponse>

    suspend fun getQuote(id : String) : QuoteIdResponse
}