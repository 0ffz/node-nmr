package me.dvyy.nmr.signal

import me.dvyy.nmr.complex.ComplexDouble
import me.dvyy.nmr.complex.ComplexDoubleArray
import kotlin.math.exp
import kotlin.math.sin

fun ComplexDoubleArray.expApodized(
    lb: Double,
): ComplexDoubleArray {
    if (lb == 0.0) return this

    forEachIndexed { index, complex ->
        val decay = exp(-Math.PI * lb * index)
        this[index] = complex * decay
    }
    return this
}

fun ComplexDoubleArray.gaussApodized(
    a: Double,
): ComplexDoubleArray {
    if (a == 0.0) return this
    forEachIndexed { index, complex ->
        val decay = exp(-0.01 * a * (index.toLong() * index.toLong()))
        this[index] = complex * decay
    }
    return this
}

fun ComplexDoubleArray.sineBellApodized(): ComplexDoubleArray {
    val n = this.size
    forEachIndexed { index, complex ->
        val weight = sin(Math.PI * index / (n - 1))
        this[index] = complex * weight
    }
    return this
}