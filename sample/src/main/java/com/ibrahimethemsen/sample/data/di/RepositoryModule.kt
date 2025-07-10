package com.ibrahimethemsen.sample.data.di

import com.ibrahimethemsen.sample.data.repository.QuoteRepositoryImpl
import com.ibrahimethemsen.sample.domain.QuoteRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
abstract class RepositoryModule {
    @[Binds Singleton]
    abstract fun bindQuoteRepository(quoteRepositoryImpl: QuoteRepositoryImpl): QuoteRepository
}