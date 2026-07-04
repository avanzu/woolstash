package de.avanzu.woolstash.domain.model

import java.util.UUID

data class Tag(
    val name: String,
) {
    init {
        require(name.isNotBlank()) { "Tag name must not be blank." }
    }
}

data class PhotoRef(
    val id: PhotoId = PhotoId.new(),
    val caption: String? = null,
    val sortOrder: Int = 0,
)

@JvmInline
value class PhotoId(val value: String) {
    companion object {
        fun new(): PhotoId = PhotoId(UUID.randomUUID().toString())
    }
}