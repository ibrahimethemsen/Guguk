package com.ibrahimethemsen.sample.data.dto


import com.ibrahimethemsen.sample.domain.entity.QuoteIdEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class QuoteIdResponse(
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


fun QuoteIdResponse.toEntity() = QuoteIdEntity(
    author = author,
    content = content,
)