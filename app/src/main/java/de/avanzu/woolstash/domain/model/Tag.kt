package de.avanzu.woolstash.domain.model

import java.util.Locale
import java.util.UUID

data class Tag(
    val name: String,
) {
    init {
        require(name.isNotBlank()) { "Tag name must not be blank." }
    }

    companion object {
        fun fromInput(input: String): Tag? {
            val normalizedName = input
                .trim()
                .trimStart('#')
                .trim()
                .lowercase(Locale.ROOT)

            return normalizedName
                .takeIf { name -> name.isNotBlank() }
                ?.let { name -> Tag(name) }
        }
    }
}

fun Iterable<Tag>.normalizedDistinct(): List<Tag> {
    return mapNotNull { tag -> Tag.fromInput(tag.name) }
        .distinctBy { tag -> tag.name }
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
