package me.dvyy.nmr.propack

import fftw.FftwDirection
import fftw.FftwFlag
import fftw.FftwPlan1D
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

object Sizes {
    const val COMPLEX = 16L
}

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
    private val nFft = MathHelpers.nextPowerOfTwo(targetLen)

    // 2. Pre-allocate dedicated buffers for FFT operations to avoid allocations during multiply()
    private val fftIn = arena.allocate(nFft * Sizes.COMPLEX)
    private val fftOut = arena.allocate(nFft * Sizes.COMPLEX)

    // 3. Pre-computed FFTs of the defining vector
    private val fidFft = arena.allocate(nFft * Sizes.COMPLEX)
    private val fidConjFft = arena.allocate(nFft * Sizes.COMPLEX)

    // 4. Reusable forward and backward transform plans using the pre-allocated buffers
    private val planForward = FftwPlan1D(nFft, fftIn, fftOut, FftwDirection.FORWARD, FftwFlag.ESTIMATE.value)
    private val planBackward = FftwPlan1D(nFft, fftIn, fftOut, FftwDirection.BACKWARD, FftwFlag.ESTIMATE.value)

    init {
        val requiredBytes = targetLen * Sizes.COMPLEX
        if (hankelData.byteSize() < requiredBytes) {
            throw IllegalArgumentException("Hankel data segment is too small! Needs $requiredBytes bytes.")
        }

        // PRECOMPUTE: FFT(fid)
        fftIn.fill(0.toByte()) // Zero-pad the rest of the array
        fftIn.asSlice(0L, requiredBytes).copyFrom(hankelData.asSlice(0L, requiredBytes))
        planForward.execute()
        fidFft.copyFrom(fftOut)

        // PRECOMPUTE: FFT(conj(fid))
        fftIn.fill(0.toByte())
        for (i in 0 until targetLen) {
            val offset = i * Sizes.COMPLEX
            val real = hankelData.get(ValueLayout.JAVA_DOUBLE, offset)
            val imag = hankelData.get(ValueLayout.JAVA_DOUBLE, offset + 8L)
            fftIn.set(ValueLayout.JAVA_DOUBLE, offset, real)
            fftIn.set(ValueLayout.JAVA_DOUBLE, offset + 8L, -imag) // Complex conjugate
        }
        planForward.execute()
        fidConjFft.copyFrom(fftOut)
    }

    override fun multiply(
        transpose: Boolean,
        rows: Int,
        cols: Int,
        input: MemorySegment,
        output: MemorySegment,
        zParm: MemorySegment,
        iParm: MemorySegment,
    ) {
        val outLen = if (transpose) cols else rows
        val inLen = if (transpose) rows else cols

        val x = input.reinterpret(inLen * Sizes.COMPLEX)
        val y = output.reinterpret(outLen * Sizes.COMPLEX)

        // Slicing offset identical to cols - 1 or rows - 1 based on translation logic
        val validStart = inLen - 1
        val precomputedFftData = if (transpose) fidConjFft else fidFft

        // 1. Prepare input: Copy conj(input) padded with zeros up to nFft
        fftIn.fill(0.toByte())
        for (i in 0 until inLen) {
            val offset = i * Sizes.COMPLEX
            val real = x.get(ValueLayout.JAVA_DOUBLE, offset)
            val imag = x.get(ValueLayout.JAVA_DOUBLE, offset + 8L)
            fftIn.set(ValueLayout.JAVA_DOUBLE, offset, real)
            fftIn.set(ValueLayout.JAVA_DOUBLE, offset + 8L, -imag) // Conjugate
        }

        // 2. Compute FFT of conj(input) -> Output placed in `fftOut`
        planForward.execute()

        // 3. Multiply precomputed FFT by conj(FFT(input)) directly in the frequency domain
        //    (Placing the result back into `fftIn` ready for the IFFT step)
        for (i in 0 until nFft) {
            val offset = i * Sizes.COMPLEX

            val aReal = precomputedFftData.get(ValueLayout.JAVA_DOUBLE, offset)
            val aImag = precomputedFftData.get(ValueLayout.JAVA_DOUBLE, offset + 8L)

            val vReal = fftOut.get(ValueLayout.JAVA_DOUBLE, offset)
            val vImag = fftOut.get(ValueLayout.JAVA_DOUBLE, offset + 8L)

            // Evaluate c = a * conj(v).
            // Expanding algebraically maps directly to this:
            val cReal = (aReal * vReal) + (aImag * vImag)
            val cImag = (aImag * vReal) - (aReal * vImag)

            fftIn.set(ValueLayout.JAVA_DOUBLE, offset, cReal)
            fftIn.set(ValueLayout.JAVA_DOUBLE, offset + 8L, cImag)
        }

        // 4. Compute IFFT -> Output placed back in `fftOut`
        planBackward.execute()

        // 5. Slice the valid window block and normalize
        //    (Unlike scipy.fft.ifft, FFTW unscaled backward needs manual dividing by N)
        val scale = 1.0 / nFft
        for (i in 0 until outLen) {
            val outOffset = i * Sizes.COMPLEX
            val inOffset = (validStart + i) * Sizes.COMPLEX

            val real = fftOut.get(ValueLayout.JAVA_DOUBLE, inOffset) * scale
            val imag = fftOut.get(ValueLayout.JAVA_DOUBLE, inOffset + 8L) * scale

            y.set(ValueLayout.JAVA_DOUBLE, outOffset, real)
            y.set(ValueLayout.JAVA_DOUBLE, outOffset + 8L, imag)
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