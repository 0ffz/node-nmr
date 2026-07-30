package me.dvyy.nmr.bindings.propack

import java.lang.foreign.MemorySegment

/**
 * Defines matrix-vector multiplication, used by propack to calculate SVD.
 *
 * Fortran signature: `SUBROUTINE APROD(TRANSA,M,N,X,Y,ZPARM,IPARM)`
 */
fun interface LinearOperator {
    /**
     * Computes `Y = A * X` (if transpose is false) or `Y = A^H * X` (if transpose is true).
     * X is supplied as [input] and Y should be written to [output]
     *
     * @param transpose If true, apply the conjugate transpose (adjoint).
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
        iParm: MemorySegment,
    )
}