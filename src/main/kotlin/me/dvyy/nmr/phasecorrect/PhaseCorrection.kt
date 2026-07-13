package me.dvyy.nmr.phasecorrect

import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.complex.exp
import me.dvyy.nmr.complex.j


fun ComplexDoubleArray.phaseCorrect(p0: Number, p1: Number): ComplexDoubleArray {
    val orig = this
    val p0Rad = Math.toRadians(p0.toDouble())
    val p1Rad = Math.toRadians(p1.toDouble())

    return ComplexDoubleArray(size) { i ->
        val phaseAngle = p0Rad + i * p1Rad / size
        val phaseMultiplier = exp(1.j * phaseAngle)
        orig[i] * phaseMultiplier
    }
}