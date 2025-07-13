package com.ibrahimethemsen.sample.presentation.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ibrahimethemsen.sample.domain.QuoteRepository
import com.ibrahimethemsen.sample.domain.entity.QuoteIdEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val quoteRepository: QuoteRepository,
) : ViewModel() {

    private val _quote = MutableStateFlow<QuoteIdEntity?>(null)
    val quote: StateFlow<QuoteIdEntity?> = _quote.asStateFlow()

    fun getQuote(id: String) {
        viewModelScope.launch {
            quoteRepository.getQuote(id).collect {
                _quote.value = it
            }
        }
    }

}