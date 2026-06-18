package me.dvyy.nmr.signal

import me.dvyy.nmr.complex.ComplexDoubleArray

fun ComplexDoubleArray.fftShift(): ComplexDoubleArray {
    val n = this.size
    val half = n / 2
    val shifted = ComplexDoubleArray(n)

    var index = 0
    // Move the second half to the front, and the first half to the back
    for (i in half until n) shifted[index++] = this[i]
    for (i in 0 until half) shifted[index++] = this[i]

    return shifted
}