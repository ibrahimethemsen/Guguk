package com.ibrahimethemsen.sample.domain

import com.ibrahimethemsen.sample.domain.entity.QuoteRandomEntity
import kotlinx.coroutines.flow.Flow

interface QuoteRepository {
    fun getRandomQuote(minLength: Int, maxLength: Int): Flow<QuoteRandomEntity>
}