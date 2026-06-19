package me.dvyy.nmr.bindings.propack

import java.lang.foreign.ValueLayout

val diagonalAprod = AprodOperator { _, _, outputLength, x, y, _, _ ->
    // For a square diagonal matrix, we just iterate up to the output size
    for (i in 0 until outputLength) {
        val diagValue = (i + 1).toDouble()

        val offsetReal = i * 16L
        val offsetImag = offsetReal + 8L

        // 3. Now it is safe to read from the reinterpreted 'x'
        val xReal = x.get(ValueLayout.JAVA_DOUBLE, offsetReal)
        val xImag = x.get(ValueLayout.JAVA_DOUBLE, offsetImag)

        val yReal = xReal * diagValue
        val yImag = xImag * diagValue

        // 4. And safe to write to the reinterpreted 'y'
        y.set(ValueLayout.JAVA_DOUBLE, offsetReal, yReal)
        y.set(ValueLayout.JAVA_DOUBLE, offsetImag, yImag)
    }
}