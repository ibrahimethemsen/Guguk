package com.ibrahimethemsen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibrahimethemsen.sample.domain.QuoteRepository
import com.ibrahimethemsen.sample.domain.entity.QuoteRandomEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GugukViewModel @Inject constructor(
    private val quoteRepository: QuoteRepository
) : ViewModel() {
    private val _refreshTrigger = MutableSharedFlow<Unit>(replay = 0, extraBufferCapacity = 1)
    private val refreshTrigger = _refreshTrigger.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val randomQuote: StateFlow<QuoteRandomEntity> = refreshTrigger
        .onStart { emit(Unit) }
        .flatMapLatest {
            quoteRepository.getRandomQuote(150, 300)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = QuoteRandomEntity("", "", emptyList())
        )

    fun requestNewQuote() {
        viewModelScope.launch {
            _refreshTrigger.tryEmit(Unit)
        }
    }


}