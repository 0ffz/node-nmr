package me.dvyy.nmr

import me.dvyy.nmr.bindings.wavelib.StationaryWaveletTransform
import me.dvyy.nmr.wavelet.WaveletHelpers.softThreshold
import org.junit.jupiter.api.Test
import kotlin.math.abs
import kotlin.use

class WaveletTests {
    @Test
    fun wavelets() {
        val N = 256

        // 1. Generate some dummy data (equivalent to reading signal.txt)
        val inputSignal = DoubleArray(N) { i -> Math.sin(i * 0.1) }

        // 2. Perform Transform safely utilizing Kotlin's `use` to prevent memory leaks
        StationaryWaveletTransform(waveletName = "bior3.5", signalLength = N, level = 1).use { swt ->

            // Forward Transform
            val coefficients = swt.forward(inputSignal)
            println("SWT Output First 5 coeffs: ${coefficients.take(5)}")

            // Inverse Transform
            val reconstructed = swt.inverse()

            // Check difference / absolute max
            var maxDiff = 0.0
            for (i in 0 until N) {
                val diff = abs(reconstructed[i] - inputSignal[i])
                if (diff > maxDiff) maxDiff = diff
            }

            println("Max Reconstruction Difference: $maxDiff")
            // If successful, diff should be close to 0.0
        }
    }

    @Test
    fun denoising() {
        val N = 256
        val J = 3 // 3 levels of decomposition

        // 1. Imagine this is your noisy signal
        val noisySignal = DoubleArray(N) { i -> Math.sin(i * 0.1) + (Math.random() * 0.5 - 0.25) }

        StationaryWaveletTransform(waveletName = "db2", signalLength = N, level = J).use { swt ->

            // 2. Forward Transform
            val coeffs = swt.forward(noisySignal)

            // 3. Denoising Setup
            val denoisedCoeffs = DoubleArray(coeffs.size)
            val threshold = 0.4 // In a real app, calculate this using universal thresholding (e.g., Donoho-Johnstone)

            // 4. Apply thresholding ONLY to detail coefficients
            for (i in coeffs.indices) {
                if (i < swt.signalLength) {
                    // Keep Approximation coefficients exactly as they are
                    denoisedCoeffs[i] = coeffs[i]
                } else {
                    // Apply soft thresholding to Detail coefficients
                    denoisedCoeffs[i] = softThreshold(coeffs[i], threshold)
                }
            }

            // 5. Inverse Transform using the denoised coefficients
            val cleanSignal = swt.inverse(denoisedCoeffs)

            println("Denoising complete. Clean signal extracted.")
        }
    }
}
