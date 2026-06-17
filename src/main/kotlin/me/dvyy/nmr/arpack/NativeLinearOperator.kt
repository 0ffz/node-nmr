package me.dvyy.nmr.arpack

import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

interface NativeLinearOperator {
    val rows: Int
    val cols: Int

    /**
     * Computes y = A^T * A * x
     * @param x Native memory segment containing the input vector (size: cols)
     * @param y Native memory segment to write the output vector (size: cols)
     */
    fun applyAtA(x: MemorySegment, y: MemorySegment)
}