package com.ibrahimethemsen.sample.domain

import com.ibrahimethemsen.sample.domain.entity.QuoteIdEntity
import com.ibrahimethemsen.sample.domain.entity.QuoteRandomEntity
import kotlinx.coroutines.flow.Flow

interface QuoteRepository {
    fun getRandomQuote(limit : Int): Flow<List<QuoteRandomEntity>>

    fun getQuote(id : String) : Flow<QuoteIdEntity>
}