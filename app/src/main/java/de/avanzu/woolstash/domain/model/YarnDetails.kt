package de.avanzu.woolstash.domain.model

data class YarnDetails(
    val length: Length? = null,
    val lengthBasis: LengthBasis? = null,
    val yarnWeight: YarnWeight? = null,
    val skeinCount: Int? = null,
    val recommendedNeedleSize: NeedleSize? = null,
    val gauge: Gauge? = null,
    val dyeLot: String? = null,
    val ply: Int? = null,
    val twistDirection: TwistDirection? = null,
) : ProductDetails {
    override val productType: ProductType = ProductType.Yarn
}

enum class LengthBasis {
    Total,
    PerUnit,
    PerHundredGrams,
}

enum class YarnWeight {
    Lace,
    Fingering,
    Sport,
    DK,
    Worsted,
    Aran,
    Bulky,
    SuperBulky,
    Unknown,
}

enum class TwistDirection {
    S,
    Z,
    Unknown,
}