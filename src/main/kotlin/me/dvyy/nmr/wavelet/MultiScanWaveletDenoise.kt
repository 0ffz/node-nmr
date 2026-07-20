package me.dvyy.nmr.wavelet

import me.dvyy.nmr.bindings.wavelib.StationaryWaveletTransform


object MultiScanWaveletDenoise {
    fun swt(scan: DoubleArray, level: Int): DoubleArray = StationaryWaveletTransform(waveletName = "db2", signalLength = scan.size, level = level).use { swt ->
        swt.forward(scan)
    }

    fun iswt(scan: DoubleArray, level: Int): DoubleArray = StationaryWaveletTransform(waveletName = "db2", signalLength = scan.size / (level + 1), level = level).use { swt ->
        swt.inverse(scan)
    }


    /**
     * Denoises a list of NMR spectra using Ensemble Variance-Mapped Wavelet Thresholding (EVWT).
     * Optimized for wavelib's flat SWT output format: [Approx, Detail1, Detail2, ..., DetailL].
     *
     * @param input List of DoubleArrays, where each array is the real part of an FFT scan.
     * @return A single DoubleArray representing the denoised and averaged spectrum.
     */
    fun denoise(input: List<DoubleArray>): DoubleArray {
        require(input.isNotEmpty()) { "Input scan list cannot be empty" }

        val n = input[0].size
        val numScans = input.size
        val level = 3 // Default decomposition level

        // Pre-allocate accumulators for the single Approximation block and 'level' Detail blocks.
        // We only need Sum and SumOfSquares to compute Mean and Variance in one pass.
        val sumApprox = DoubleArray(n)
        val sumDetail = Array(level) { DoubleArray(n) }
        val sumSqDetail = Array(level) { DoubleArray(n) }

        // Step 1 & 2: SWT Decomposition and Ensemble Accumulation
        // We iterate through all scans, decompose, and accumulate statistics directly.
        for (scan in input) {
            val flatSwt = swt(scan, level) // Expected layout: [A, D1, D2, ..., DL]

            // Accumulate Approximation (Block 0)
            for (i in 0 until n) {
                sumApprox[i] += flatSwt[i]
            }

            // Accumulate Details (Blocks 1 to level)
            for (l in 0 until level) {
                val offset = (l + 1) * n
                for (i in 0 until n) {
                    val dVal = flatSwt[offset + i]
                    sumDetail[l][i] += dVal
                    sumSqDetail[l][i] += dVal * dVal
                }
            }
        }

        // Step 3: Calculate Averaged Approximation and Denoised Details
        val flatDenoised = DoubleArray(n * (level + 1))

        // Average the approximation coefficients and place them in the output array
        for (i in 0 until n) {
            flatDenoised[i] = sumApprox[i] / numScans
        }

        // Step 4: Point-wise Variance Mapping & Bayesian Shrinkage for Details
        for (l in 0 until level) {
            val outOffset = (l + 1) * n
            for (i in 0 until n) {
                val sumD = sumDetail[l][i]
                val sumSqD = sumSqDetail[l][i]

                val meanD = sumD / numScans

                // Calculate ensemble sample variance: Var = (SumSq - n * mean^2) / (n - 1)
                // If numScans == 1, variance is 0 (leaves detail untouched).
                val variance = if (numScans > 1) {
                    val v = (sumSqD - numScans * meanD * meanD) / (numScans - 1)
                    // Clamp to 0 to prevent negative variance due to floating-point inaccuracies
                    if (v > 0.0) v else 0.0
                } else {
                    0.0
                }

                // Bayesian shrinkage weight: D_avg^2 / (D_avg^2 + sigma^2)
                val meanDSq = meanD * meanD
                val weight = if (meanDSq + variance > 1e-12) {
                    meanDSq / (meanDSq + variance)
                } else {
                    0.0
                }

                flatDenoised[outOffset + i] = meanD * weight
            }
        }

        // Step 5: Wavelet Reconstruction (ISWT)
        return iswt(flatDenoised, level)
    }
}