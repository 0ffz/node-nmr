package me.dvyy.nmr.bindings.propack

/** Target for the singular triplets. */
enum class SingularTripletTarget(val code: Byte) {
    LARGEST('L'.code.toByte()),
    SMALLEST('S'.code.toByte())
}