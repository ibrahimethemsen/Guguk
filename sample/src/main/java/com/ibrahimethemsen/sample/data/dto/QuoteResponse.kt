package com.ibrahimethemsen.sample.data.dto


import com.ibrahimethemsen.sample.domain.entity.QuoteRandomEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuoteResponse(
    @SerialName("author")
    val author: String,
    @SerialName("authorSlug")
    val authorSlug: String,
    @SerialName("content")
    val content: String,
    @SerialName("dateAdded")
    val dateAdded: String,
    @SerialName("dateModified")
    val dateModified: String,
    @SerialName("_id")
    val id: String,
    @SerialName("length")
    val length: Int,
    @SerialName("tags")
    val tags: List<String>
)

fun QuoteResponse.toEntity() = QuoteRandomEntity(
    author = author,
    content = content,
    tags = tags
)