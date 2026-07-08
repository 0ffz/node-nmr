package me.dvyy.nmr.wavelet

import me.dvyy.nmr.bindings.wavelib.StationaryWaveletTransform
import kotlin.use

object WaveletHelpers {
    // Helper function for Soft Thresholding
    fun softThreshold(value: Double, threshold: Double): Double {
        return when {
            value > threshold -> value - threshold
            value < -threshold -> value + threshold
            else -> 0.0
        }
    }

    fun DoubleArray.applySoftThreshold(start: Int = 0, threshold: Double) {
        for (i in start..lastIndex) {
            this[i] = softThreshold(this[i], threshold)
        }
    }

    fun waveletDenoise(input: DoubleArray): DoubleArray {
        return StationaryWaveletTransform(waveletName = "db2", signalLength = input.size, level = 4).use { swt ->
            val waveletIm = swt.forward(input)
            waveletIm.applySoftThreshold(start = input.size,0.1)
            swt.inverse(waveletIm)
        }
    }
}
