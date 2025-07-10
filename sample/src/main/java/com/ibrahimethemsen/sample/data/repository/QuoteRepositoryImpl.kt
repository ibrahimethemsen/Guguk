package com.ibrahimethemsen.sample.data.repository

import com.ibrahimethemsen.sample.data.dto.toEntity
import com.ibrahimethemsen.sample.data.source.QuoteRemoteDataSource
import com.ibrahimethemsen.sample.domain.QuoteRepository
import com.ibrahimethemsen.sample.domain.entity.QuoteRandomEntity
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class QuoteRepositoryImpl @Inject constructor(
    private val quoteRemoteDataSource: QuoteRemoteDataSource
) : QuoteRepository {
    override fun getRandomQuote(
        minLength: Int,
        maxLength: Int
    ): Flow<QuoteRandomEntity> = flow {
        emit(quoteRemoteDataSource.getRandomQuote(minLength, maxLength).toEntity())
    }
}