package me.dvyy.nmr.processing.evaluation

import kotlin.math.abs
import kotlin.math.pow

/**
 * Structural similarity index implementation based on
 * [DESPARATE](https://github.com/rschurko/DESPERATE/blob/master/simulations/simpson.py)
 */
object SSIM {
    const val K1 = 0.01
    const val K2 = 0.03

    /**
     * Define a local window size (e.g., 11, 21, or 51 data points).
     * Extract that small slice from both the clean signal $x$ and denoised signal $y$.
     * Calculate the local $\mu, \sigma^2,$ and $\sigma_{xy}$ for those slices and compute the local SSIM.
     * Slide the window over by one data point and repeat the process across the entire spectrum.
     * Average all the local SSIM values to get the Mean SSIM (MSSIM).
     */
    fun windowed(x: DoubleArray, y: DoubleArray, windowSize: Int = 11): Double {
        val dynamicRange = (x.max() - x.min())
        require(x.size == y.size) { "Signals must have the same length" }
        if (x.size < windowSize) return of(x, y, dynamicRange)

        var totalSSIM = 0.0
        val numWindows = x.size - windowSize + 1

        for (i in 0 until numWindows) {
            val sliceX = x.copyOfRange(i, i + windowSize)
            val sliceY = y.copyOfRange(i, i + windowSize)
            totalSSIM += of(sliceX, sliceY, dynamicRange)
        }

        return totalSSIM / numWindows
    }

    fun of(x: DoubleArray, y: DoubleArray, dynamicRange: Double): Double {
        // 1. Safety checks MUST happen first to prevent .max() crashes
        require(x.size == y.size) { "Signals must have the same length" }
        val n = x.size

        // SSIM requires at least 2 points to calculate sample covariance
        if (n <= 1) return Double.NaN

        // 2. Establish Global Dynamic Range (L)
        // Prefer passed-in global range, fallback to empirical range of 'x'
        val l = dynamicRange

        // Handle edge case where flat signal results in L = 0
        val safeL = if (l == 0.0) 1.0 else l

        val c1 = (K1 * safeL).pow(2.0)
        val c2 = (K2 * safeL).pow(2.0)

        // 3. Calculate Means
        val meanX = x.average()
        val meanY = y.average()

        var sumSqX = 0.0
        var sumSqY = 0.0
        var sumCovXY = 0.0

        // 4. Calculate sums for variance and covariance
        for (i in 0 until n) {
            val dx = x[i] - meanX
            val dy = y[i] - meanY
            sumSqX += dx * dx
            sumSqY += dy * dy
            sumCovXY += dx * dy
        }

        // 5. Apply CONSISTENT degrees of freedom (n - 1 for sample statistics)
        val degreesOfFreedom = (n - 1).toDouble()
        val varX = sumSqX / degreesOfFreedom
        val varY = sumSqY / degreesOfFreedom
        val covXY = sumCovXY / degreesOfFreedom

        // 6. Calculate SSIM
        val numerator = (2 * meanX * meanY + c1) * (2 * covXY + c2)
        val denominator = (meanX * meanX + meanY * meanY + c1) * (varX + varY + c2)

        // 7. Safer Floating Point comparison
        return if (abs(denominator) < 1e-10) {
            if (abs(numerator) < 1e-10) 1.0 else 0.0
        } else {
            numerator / denominator
        }
    }
}