package me.dvyy.nmr.signal

import me.dvyy.nmr.complex.ComplexDouble
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.complex.toComplexArray
import kotlin.math.exp

fun ComplexDoubleArray.expApodization(lb: Double): ComplexDoubleArray {
    return this.mapIndexed { index, complex ->
        val decay = exp(-Math.PI * lb * index)
        ComplexDouble(complex.re * decay, complex.im * decay)
    }.toComplexArray()
}