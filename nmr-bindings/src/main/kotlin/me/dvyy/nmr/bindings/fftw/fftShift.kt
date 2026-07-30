package me.dvyy.nmr.bindings.fftw

import me.dvyy.nmr.common.math.ComplexDoubleArray

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

fun ComplexDoubleArray.inverseFftShift(): ComplexDoubleArray {
    val n = this.size
    val split = n - n / 2
    val shifted = ComplexDoubleArray(n)

    var index = 0
    // Move the second part to the front, and the first part to the back
    for (i in split until n) shifted[index++] = this[i]
    for (i in 0 until split) shifted[index++] = this[i]

    return shifted
}
