package me.dvyy.nmr.processing.denoise.cadzow

import me.dvyy.nmr.bindings.propack.SVDResult
import me.dvyy.nmr.common.math.ComplexDouble
import me.dvyy.nmr.common.math.ComplexDoubleArray

/**
 * sum on anti-diagonals
 * [ 1, 2, 3 ]
 * [ 4, 5, 6 ]
 * [ 7, 8, 9 ]
 * => [1, 4 + 2, 7 + 5 + 3, 8 + 6, 9]
 */
fun SVDResult.reconstructDiagonalsSlow(): ComplexDoubleArray {
//    val sizeU = u.rows
//    val sizeV = v.rows
    val numDiagonals = rows + cols - 1
    val reconstructedArray = ComplexDoubleArray(numDiagonals)

    for (diagonal in 0 until numDiagonals) {
        val rStart = (diagonal - cols + 1).coerceAtLeast(0)
        val rEnd = diagonal.coerceAtMost(rows - 1)
        // The number of elements on this specific anti-diagonal
        val count = rEnd - rStart + 1

        var diagonalSum = ComplexDouble.zero

        // Compute the contribution of each eigenvector to this diagonal
        for (i in singularValues.indices) {
            val sigma = singularValues[i]
            val vectorU = u[i]
            val vectorV = v[i]
            var eigenProductSum = ComplexDouble.zero

            // reconstruct by taking outer product, for [a,b,c], on the 2th diagonal looks like:
            // [ aa,  ab , {ac}]
            // [ ba, {bb},  bc ]
            // [{ca}, cb ,  cc ]
            // Which summing is ca * bb * ac
            // which is (a, b, c) dot (c, b, a)
            for (row in rStart..rEnd) {
                eigenProductSum += vectorU[row] * vectorV[diagonal - row].conjugate() // we use transpose conjugate
            }

            diagonalSum += eigenProductSum * sigma
        }

        // Average it out immediately
        reconstructedArray[diagonal] = diagonalSum / count.toDouble()
    }
    return reconstructedArray
}