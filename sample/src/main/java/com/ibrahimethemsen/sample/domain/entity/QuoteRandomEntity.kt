package com.ibrahimethemsen.sample.domain.entity

data class QuoteRandomEntity(
    val id : String,
    val author: String,
    val content: String,
    val tags: List<String>
)
