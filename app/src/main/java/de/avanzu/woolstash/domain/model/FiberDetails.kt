package de.avanzu.woolstash.domain.model

data class FiberDetails(
    val fiberForm: FiberForm? = null,
    val preparation: FiberPreparation? = null,
    val breedOrSource: String? = null,
    val stapleLength: Length? = null,
    val micron: Double? = null,
    val intendedSpin: String? = null,
) : ProductDetails {
    override val productType: ProductType = ProductType.Fiber
}

enum class FiberForm {
    Top,
    Roving,
    Batt,
    Rolag,
    Fleece,
    Locks,
    Other,
    Unknown,
}

enum class FiberPreparation {
    Raw,
    Washed,
    Carded,
    Combed,
    Dyed,
    Natural,
    Blended,
    Other,
    Unknown,
}