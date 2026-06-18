package me.dvyy.nmr.svd

import fftw.FftwComplexArray
import fftw.FftwDirection
import fftw.FftwFlag
import fftw.FftwPlan1D
import me.dvyy.nmr.complex.ComplexDouble
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.propack.MathHelpers
import me.dvyy.nmr.propack.SVDResult
import me.dvyy.nmr.propack.Sizes
import java.lang.foreign.Arena

context(arena: Arena)
fun SVDResult.reconstructDiagonals(): ComplexDoubleArray {val numDiagonals = rows + cols - 1
    val size = MathHelpers.nextPowerOfTwo(numDiagonals)

    // 1. Allocate Workspaces Once
    // Note: arena.allocate() guarantees zero-initialized memory in Java FFM.
    val uTime = FftwComplexArray(size)
    val uFreq = FftwComplexArray(size)
    val vTime = FftwComplexArray(size)
    val vFreq = FftwComplexArray(size)

    val accumFreq = FftwComplexArray(size)
    val resultTime = FftwComplexArray(size)

    // 2. Create Plans (FftwFlag.ESTIMATE is safe to use before populating data)
    val planU = FftwPlan1D(size, uTime.segment, uFreq.segment, FftwDirection.FORWARD, FftwFlag.ESTIMATE.value)
    val planV = FftwPlan1D(size, vTime.segment, vFreq.segment, FftwDirection.FORWARD, FftwFlag.ESTIMATE.value)
    val planIfft = FftwPlan1D(size, accumFreq.segment, resultTime.segment, FftwDirection.BACKWARD, FftwFlag.ESTIMATE.value)

    // 3. Process Each Singular Component into the Accumulator
    for (i in singularValues.indices) {
        val sigma = singularValues[i]
        val vectorU = u[i]
        val vectorV = v[i]

        // Populate U (Zero-padding is handled because arena memory starts as 0.0)
        uTime.loadInterleaved(vectorU.data)
        // Clear previous padding in uTime if reusing buffer
        for (j in rows until size) {
            uTime.set(j, 0.0, 0.0)
        }

        // Populate V (Conjugate the imaginary part! This is required for Hankelization)
        for (j in 0 until cols) {
            vTime.set(j, vectorV.getRe(j), -vectorV.getIm(j)) // Conjugate
        }
        // Clear previous padding in vTime
        for (j in cols until size) {
            vTime.set(j, 0.0, 0.0)
        }

        // Execute Forward FFTs
        planU.execute()
        planV.execute()

        // Pointwise multiply, scale by sigma, and accumulate: accumFreq += sigma * (U_freq * V_freq)
        for (j in 0 until size) {
            val (uRe, uIm) = uFreq.get(j)
            val (vRe, vIm) = vFreq.get(j)

            // Complex multiplication
            val multRe = uRe * vRe - uIm * vIm
            val multIm = uRe * vIm + uIm * vRe

            // Accumulate
            accumFreq.set(j, accumFreq.get(j).real + multRe * sigma, accumFreq.get(j).imag + multIm * sigma)
        }
    }

    // 4. One Inverse FFT to rule them all
    planIfft.execute()

    // 5. Extract, Truncate, and Normalize
    val reconstructedArray = ComplexDoubleArray(numDiagonals)

    for (diagonal in 0 until numDiagonals) {
        val rStart = (diagonal - cols + 1).coerceAtLeast(0)
        val rEnd = diagonal.coerceAtMost(rows - 1)
        val count = rEnd - rStart + 1

        // FFTW does not normalize the backwards transform by default.
        // We divide by 'size' for IFFT normalization, AND by 'count' to average the anti-diagonal.
        val normFactor = size.toDouble() * count.toDouble()

        val re = resultTime.get(diagonal).real / normFactor
        val im = resultTime.get(diagonal).imag / normFactor

        reconstructedArray[diagonal] = ComplexDouble(re, im)
    }

    // Optional: If your FftwPlan1D wrapper doesn't hook into the Arena Cleaner,
    // you must explicitly destroy the FFTW plans to prevent native memory leaks.
    // planU.destroy()
    // planV.destroy()
    // planIfft.destroy()

    return reconstructedArray
}

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