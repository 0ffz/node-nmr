package me.dvyy.nmr.signal

import me.dvyy.nmr.complex.ComplexDouble
import me.dvyy.nmr.complex.ComplexDoubleArray
import kotlin.math.exp

fun ComplexDoubleArray.expApodized(
    lb: Double,
): ComplexDoubleArray {
    if (lb == 0.0) return this

    forEachIndexed { index, complex ->
        val decay = exp(-Math.PI * lb * index)
        this[index] = ComplexDouble(complex.re * decay, complex.im * decay)
    }
    return this
}

fun ComplexDoubleArray.gaussApodized(
    lb: Double,
): ComplexDoubleArray {
    forEachIndexed { index, complex ->
        val decay = exp(-Math.PI * lb * (index.toLong() * index.toLong()))
        this[index] = ComplexDouble(complex.re * decay, complex.im * decay)
    }
    return this
}