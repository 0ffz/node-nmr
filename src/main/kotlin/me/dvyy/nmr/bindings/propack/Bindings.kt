package me.dvyy.nmr.bindings.propack

/** Target for the singular triplets. */
enum class SingularTripletTarget(val code: Byte) {
    LARGEST('L'.code.toByte()),
    SMALLEST('S'.code.toByte())
}

/** Determines if singular vectors should be computed. */
enum class ComputeVectors(val code: Byte) {
    YES('Y'.code.toByte()),
    NO('N'.code.toByte())
}

