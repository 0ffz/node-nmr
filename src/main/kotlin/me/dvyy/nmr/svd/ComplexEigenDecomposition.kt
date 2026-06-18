package me.dvyy.nmr.svd

import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.complex.asComplexInterweaved

data class ComplexEigenDecomposition(
    val eigenvalues: ComplexDoubleArray,
    val eigenvectors: List<ComplexDoubleArray>,
) {
    companion object {
        fun of(
            eigenvalues: DoubleArray,
            eigenvectors: DoubleArray,
        ): ComplexEigenDecomposition {
            val count = eigenvalues.size / 2
            val eigenvectorLength = (eigenvectors.size / count)
            val complexEigenvalues = eigenvalues.asComplexInterweaved()
            val complexEigenvectors = Array(count) { i ->
                val start = eigenvectorLength * i
                val end = start + eigenvectorLength
                eigenvectors.copyOfRange(start, end).asComplexInterweaved()
            }.toList()
            return ComplexEigenDecomposition(complexEigenvalues, complexEigenvectors)
        }
    }
}

