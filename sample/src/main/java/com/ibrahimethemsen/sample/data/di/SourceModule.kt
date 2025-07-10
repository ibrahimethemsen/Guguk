package com.ibrahimethemsen.sample.data.di

import com.ibrahimethemsen.sample.data.source.QuoteRemoteDataSource
import com.ibrahimethemsen.sample.data.source.QuoteRemoteDataSourceImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@[Module InstallIn(SingletonComponent::class)]
abstract class SourceModule {
    @[Binds Singleton]
    abstract fun bindQuoteDataSource(quoteRemoteDataSourceImpl: QuoteRemoteDataSourceImpl): QuoteRemoteDataSource
}