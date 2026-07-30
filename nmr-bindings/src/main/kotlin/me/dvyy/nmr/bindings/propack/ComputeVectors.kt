package me.dvyy.nmr.bindings.propack

/** Determines if singular vectors should be computed. */
enum class ComputeVectors(val code: Byte) {
    YES('Y'.code.toByte()),
    NO('N'.code.toByte())
}