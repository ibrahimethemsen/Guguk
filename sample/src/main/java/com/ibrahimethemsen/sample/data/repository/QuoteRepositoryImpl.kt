package com.ibrahimethemsen.sample.data.repository

import com.ibrahimethemsen.sample.data.dto.toEntity
import com.ibrahimethemsen.sample.data.source.QuoteRemoteDataSource
import com.ibrahimethemsen.sample.domain.QuoteRepository
import com.ibrahimethemsen.sample.domain.entity.QuoteIdEntity
import com.ibrahimethemsen.sample.domain.entity.QuoteRandomEntity
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class QuoteRepositoryImpl @Inject constructor(
    private val quoteRemoteDataSource: QuoteRemoteDataSource
) : QuoteRepository {
    override fun getRandomQuote(
        limit : Int,
    ): Flow<List<QuoteRandomEntity>> = flow {
        emit(quoteRemoteDataSource.getRandomQuote(limit).map { it.toEntity() })
    }

    override fun getQuote(id: String): Flow<QuoteIdEntity> = flow{
        emit(quoteRemoteDataSource.getQuote(id).toEntity())
    }
}