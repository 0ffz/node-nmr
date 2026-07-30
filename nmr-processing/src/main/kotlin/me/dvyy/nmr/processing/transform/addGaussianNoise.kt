package me.dvyy.nmr.processing.transform

import me.dvyy.nmr.common.math.ComplexDouble
import me.dvyy.nmr.common.math.ComplexDoubleArray
import java.util.Random

fun ComplexDoubleArray.addGaussianNoise(strength: Double, seed: Long? = null): ComplexDoubleArray {
    val random = if (seed != null) Random(seed) else Random()
    return this.mapComplex {
        ComplexDouble(it.re + strength * (random.nextDouble() - 0.5), it.im + strength * (random.nextDouble() - 0.5))
    }
}