package me.dvyy.nmr.svd

import me.dvyy.nmr.bindings.fftw.FftwComplexArray
import me.dvyy.nmr.bindings.fftw.FftwDirection.BACKWARD
import me.dvyy.nmr.bindings.fftw.FftwDirection.FORWARD
import me.dvyy.nmr.bindings.fftw.FftwFlag
import me.dvyy.nmr.bindings.fftw.FftwPlan1D
import me.dvyy.nmr.bindings.helpers.Sizes
import me.dvyy.nmr.bindings.propack.AprodOperator
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Creates an AprodOperator for a complex Hankel matrix utilizing FFT convolution.
 * * @param hankelData A MemorySegment containing the complex double defining vector `fid`.
 * It must contain at least (M + N - 1) complex doubles.
 * Stored interleaved: [v0_real, v0_imag, v1_real, v1_imag, ...]
 */
class HankelOperator(
    private val arena: Arena,
    private val hankelData: MemorySegment,
    private val rows: Int,
    private val cols: Int,
) : AprodOperator {

    // 1. Calculate length and optimal padding length (next power of 2 for maximum FFT efficiency)
    private val targetLen = rows + cols - 1
    private val fftLength = MathHelpers.nextPowerOfTwo(targetLen)

    // 2. Pre-allocate dedicated buffers for FFT operations to avoid allocations during multiply()
    private val fftIn = with(arena) { FftwComplexArray.alloc(fftLength) }
    private val fftOut = with(arena) { FftwComplexArray.alloc(fftLength) }

    // 3. Pre-computed FFTs of the defining vector
    private val fidFft = with(arena) { FftwComplexArray.alloc(fftLength) }
    private val fidConjFft = with(arena) { FftwComplexArray.alloc(fftLength) }

    // 4. Reusable forward and backward transform plans using the pre-allocated buffers
    private val planForward = FftwPlan1D(fftLength, fftIn, fftOut, FORWARD, FftwFlag.ESTIMATE)
    private val planBackward = FftwPlan1D(fftLength, fftIn, fftOut, BACKWARD, FftwFlag.ESTIMATE)

    init {
        val requiredBytes = targetLen * Sizes.COMPLEX
        if (hankelData.byteSize() < requiredBytes) {
            throw IllegalArgumentException("Hankel data segment is too small! Needs $requiredBytes bytes.")
        }

        // PRECOMPUTE: FFT(fid)
        fftIn.segment.fill(0.toByte()) // Zero-pad the rest of the array
        fftIn.segment.asSlice(0L, requiredBytes).copyFrom(hankelData.asSlice(0L, requiredBytes))
        planForward.execute()
        fidFft.segment.copyFrom(fftOut.segment)

        // PRECOMPUTE: FFT(conj(fid))
        fftIn.segment.fill(0.toByte())
        for (i in 0 until targetLen) {
            val offset = i * Sizes.COMPLEX
            val real = hankelData.get(ValueLayout.JAVA_DOUBLE, offset)
            val imag = hankelData.get(ValueLayout.JAVA_DOUBLE, offset + 8L)
            fftIn.segment.set(ValueLayout.JAVA_DOUBLE, offset, real)
            fftIn.segment.set(ValueLayout.JAVA_DOUBLE, offset + 8L, -imag) // Complex conjugate
        }
        planForward.execute()
        fidConjFft.segment.copyFrom(fftOut.segment)
    }

    override fun multiply(
        transpose: Boolean,
        inputLength: Int,
        outputLength: Int,
        input: MemorySegment,
        output: MemorySegment,
        zParm: MemorySegment,
        iParm: MemorySegment,
    ) {
        // Slicing offset identical to cols - 1 or rows - 1 based on translation logic
        val validStart = inputLength - 1
        val precomputedFftData = if (transpose) fidConjFft else fidFft
        val input = FftwComplexArray(input)
        val output = FftwComplexArray(output)

        // Copy input as reversed into fftIn, with end padding being 0s (we want a power of 2 length for a faster fft)
        fftIn.segment.fill(0.toByte())
        for (i in 0 until inputLength) {
            val real = input.getReal(i)
            val imag = input.getImag(i)
            fftIn.set(inputLength - i - 1, real, imag) // Conjugate
        }
        planForward.execute() // calculate fft to fftOut

        // Multiply fftOut and precomputed, write result to fftIn
        for (i in 0 until fftLength) {
            val aReal = precomputedFftData.getReal(i)
            val aImag = precomputedFftData.getImag(i)

            val vReal = fftOut.getReal(i)
            val vImag = fftOut.getImag(i)

            // Evaluate c = a * v
            val cReal = aReal * vReal - aImag * vImag
            val cImag = aReal * vImag + aImag * vReal

            fftIn.set(i, cReal, cImag)
        }

        // Compute inverse fft on fftIn, writes to fftOut
        planBackward.execute()

        // Extract target window, scale back since FFTW is unscaled
        val scale = 1.0 / fftLength
        for (i in 0 until outputLength) {
            val inOffset = validStart + i

            val real = fftOut.getReal(inOffset) * scale
            val imag = fftOut.getImag(inOffset) * scale

            output.set(i, real, imag)
        }
    }

}

object MathHelpers {
    /**
     * Finds the nearest fast power of two layout equivalent to SciPy's next_fast_len.
     */
    fun nextPowerOfTwo(n: Int): Int {
        var count = 0
        var value = n - 1
        while (value > 0) {
            value = value shr 1
            count++
        }
        return 1 shl count
    }
}