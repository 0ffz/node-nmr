package me.dvyy.nmr.bindings.propack

import java.lang.foreign.MemorySegment

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

/**
 * High-performance, zero-allocation callback for matrix-vector multiplication.
 * Fortran signature: SUBROUTINE APROD(TRANSA,M,N,X,Y,ZPARM,IPARM)
 */
fun interface AprodOperator {
    /**
     * Computes Y = A * X (if transpose is false) or Y = A^H * X (if transpose is true).
     * * @param transpose If true, apply the conjugate transpose (adjoint).
     * @param inputLength Number of rows in A.
     * @param outputLength Number of columns in A.
     * @param input Input vector (Complex Double Array).
     * @param output Output vector (Complex Double Array). MUST MUTATE IN PLACE.
     * @param zParm Complex double array for passing user data.
     * @param iParm Integer array for passing user data.
     */
    fun multiply(
        transpose: Boolean,
        inputLength: Int,
        outputLength: Int,
        input: MemorySegment,
        output: MemorySegment,
        zParm: MemorySegment,
        iParm: MemorySegment
    )
}