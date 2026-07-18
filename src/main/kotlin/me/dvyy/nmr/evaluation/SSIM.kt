/*
Copyright 2021	Adam Altenhof

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files (the "Software"), to deal in the Software without restriction, including without limitation the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package me.dvyy.nmr.evaluation

/**
 * Structural similarity index implementation based on
 * [DESPARATE](https://github.com/rschurko/DESPERATE/blob/master/simulations/simpson.py)
 */
object SSIM {
    /**
     * Returns SSIM(x, y) - c where `c = SSIM(y, y) - 1`, such that the output is bounded between [-1, 1]
     */
    fun bounded(x: DoubleArray, y: DoubleArray): Double {
        return of(x, y) - of(y, y) + 1
    }

    fun of(x: DoubleArray, y: DoubleArray): Double {
        require(x.size == y.size) { "Signals must have the same length" }
        val n = x.size

        // SSIM requires at least 2 points to calculate sample covariance
        if (n <= 1) return Double.NaN

        // 1. Calculate Means
        val meanX = x.average()
        val meanY = y.average()

        var sumSqX = 0.0
        var sumSqY = 0.0
        var sumCovXY = 0.0

        // 2. Calculate sums for variance and covariance in a single pass
        for (i in 0 until n) {
            val dx = x[i] - meanX
            val dy = y[i] - meanY

            sumSqX += dx * dx
            sumSqY += dy * dy
            sumCovXY += dx * dy
        }

        // 3. Apply NumPy's specific degrees of freedom (ddof) defaults
        // np.std(X)**2 uses ddof=0 (population variance)
        val varX = sumSqX / n
        val varY = sumSqY / n

        // np.cov(X,Y) uses ddof=1 (sample covariance)
        val covXY = sumCovXY / (n - 1)

        // 4. Calculate SSIM
        // Note: The original Python code had +0 for stability constants (C1, C2)
        val numerator = (2 * meanX * meanY) * (2 * covXY)
        val denominator = (meanX * meanX + meanY * meanY) * (varX + varY)

        // Avoid division by zero if both signals are completely flat/zero
        return if (denominator == 0.0) {
            if (numerator == 0.0) 1.0 else 0.0
        } else {
            numerator / denominator
        }
    }
}