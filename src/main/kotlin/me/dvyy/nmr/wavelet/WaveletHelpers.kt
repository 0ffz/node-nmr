package me.dvyy.nmr.wavelet

object WaveletHelpers {
    // Helper function for Soft Thresholding
    fun softThreshold(value: Double, threshold: Double): Double {
        return when {
            value > threshold -> value - threshold
            value < -threshold -> value + threshold
            else -> 0.0
        }
    }

    fun DoubleArray.applySoftThreshold(threshold: Double) {
        for (i in indices) {
            this[i] = softThreshold(this[i], threshold)
        }
    }
}
