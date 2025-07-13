package com.ibrahimethemsen.sample.data.source

import com.ibrahimethemsen.sample.data.dto.QuoteIdResponse
import com.ibrahimethemsen.sample.data.dto.QuoteResponse
import com.ibrahimethemsen.sample.data.service.QuoteService
import jakarta.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class QuoteRemoteDataSourceImpl @Inject constructor(
    private val quoteService: QuoteService
): QuoteRemoteDataSource {
    override suspend fun getRandomQuote(
        limit: Int,
    ): List<QuoteResponse> {
        return withContext(Dispatchers.IO) {
            quoteService.getRandomQuote(limit)
        }
    }

    override suspend fun getQuote(id: String): QuoteIdResponse {
        return withContext(Dispatchers.IO){
            quoteService.getQuote(id)
        }
    }
}