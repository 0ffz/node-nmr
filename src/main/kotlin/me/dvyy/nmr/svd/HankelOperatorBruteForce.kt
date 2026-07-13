package me.dvyy.nmr.svd

import me.dvyy.nmr.bindings.propack.LinearOperator
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * A simple but slow implementation of Hankel matrix multiplication to be able to test correctness of the sped up FFT convolution method.
 */
class HankelOperatorBruteForce(private val hankelData: MemorySegment) : LinearOperator {
    override fun multiply(
        transpose: Boolean,
        inputLength: Int,
        outputLength: Int,
        input: MemorySegment,
        output: MemorySegment,
        zParm: MemorySegment,
        iParm: MemorySegment,
    ) {
        // (Optional but recommended) Safety check to ensure hankelData is large enough
        // We need elements up to index (rows + cols - 2). So size must be (rows + cols - 1).
        val requiredBytes = (inputLength + outputLength - 1) * 16L
        if (hankelData.byteSize() < requiredBytes) {
            throw IllegalArgumentException("Hankel data segment is too small! Needs $requiredBytes bytes.")
        }

        // 3. Perform the matrix-vector multiplication Y = A*X or Y = A^H*X
        for (i in 0 until outputLength) {
            var yReal = 0.0
            var yImag = 0.0

            for (j in 0 until inputLength) {
                // For a Hankel matrix, the value at (row, col) is v[row + col].
                // If not transposed: row = i, col = j.
                // If transposed: row = j, col = i.
                // In both cases, the index into the defining vector is simply (i + j).
                val vIndex = i + j
                val vOffset = vIndex * 16L

                val vReal = hankelData.get(ValueLayout.JAVA_DOUBLE, vOffset)
                var vImag = hankelData.get(ValueLayout.JAVA_DOUBLE, vOffset + 8L)

                // If transpose is true, PROPACK wants Y = A^H * X.
                // Since a Hankel matrix is symmetric (A^T = A), A^H is simply the complex conjugate.
                if (transpose) {
                    vImag = -vImag
                }

                // Read X value
                val xOffset = j * 16L
                val xReal = input.get(ValueLayout.JAVA_DOUBLE, xOffset)
                val xImag = input.get(ValueLayout.JAVA_DOUBLE, xOffset + 8L)

                // Complex multiplication: (vReal + vImag*i) * (xReal + xImag*i)
                yReal += (vReal * xReal) - (vImag * xImag)
                yImag += (vReal * xImag) + (vImag * xReal)
            }

            // 4. Write the result directly to Y's off-heap memory
            val yOffset = i * 16L
            output.set(ValueLayout.JAVA_DOUBLE, yOffset, yReal)
            output.set(ValueLayout.JAVA_DOUBLE, yOffset + 8L, yImag)
        }
    }
}