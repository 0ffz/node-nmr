package me.dvyy.nmr.synthetic

import me.dvyy.nmr.complex.ComplexDouble
import me.dvyy.nmr.complex.ComplexDoubleArray
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sin
import java.util.Random

/**
 * Data class representing a single NMR resonance.
 */
data class Resonance(
    val amplitude: Double,
    val frequencyHz: Double,
    val phaseRadians: Double,
    val t2StarSeconds: Double,
)

/**
 * Generates a theoretical Time-Domain NMR signal (FID).
 *
 * @param numPoints The total number of complex points to generate.
 * @param dwellTimeSeconds The time interval between consecutive points (inverse of sample rate).
 * @param resonances A list of [Resonance] components making up the signal.
 * @return A [DoubleArray] of size numPoints * 2 containing interleaved [real, imaginary] values.
 */
fun generateNmrSignal(
    numPoints: Int,
    dwellTimeSeconds: Double,
    resonances: List<Resonance>,
): ComplexDoubleArray {
    // Array size is doubled to accommodate interleaved real/imaginary pairs
    val signal = DoubleArray(numPoints * 2)
    val twoPi = 2.0 * PI

    for (i in 0 until numPoints) {
        val t = i * dwellTimeSeconds
        var realSum = 0.0
        var imagSum = 0.0

        for (resonance in resonances) {
            val angularFreq = twoPi * resonance.frequencyHz
            val decayFactor = exp(-t / resonance.t2StarSeconds)
            val angle = angularFreq * t + resonance.phaseRadians
            val envelope = resonance.amplitude * decayFactor

            realSum += envelope * cos(angle)
            imagSum += envelope * sin(angle)
        }

        // Store interleaved complex numbers
        signal[i * 2] = realSum
        signal[i * 2 + 1] = imagSum
    }

    return ComplexDoubleArray(signal)
}


fun ComplexDoubleArray.addGaussianNoise(strength: Double, seed: Long? = null): ComplexDoubleArray {
    val random = if (seed != null) Random(seed) else Random()
    return this.mapComplex {
        ComplexDouble(it.re + strength * (random.nextDouble() - 0.5), it.im + strength * (random.nextDouble() - 0.5))
    }
}
