package me.dvyy.nmr

import me.dvyy.nmr.bindings.propack.LinearOperator
import java.lang.foreign.ValueLayout

val diagonalMatrixOperator = LinearOperator { _, _, outputLength, x, y, _, _ ->
    for (i in 0 until outputLength) {
        val diagValue = (i + 1).toDouble()

        val offsetReal = i * 16L
        val offsetImag = offsetReal + 8L

        val xReal = x.get(ValueLayout.JAVA_DOUBLE, offsetReal)
        val xImag = x.get(ValueLayout.JAVA_DOUBLE, offsetImag)

        val yReal = xReal * diagValue
        val yImag = xImag * diagValue

        y.set(ValueLayout.JAVA_DOUBLE, offsetReal, yReal)
        y.set(ValueLayout.JAVA_DOUBLE, offsetImag, yImag)
    }
}