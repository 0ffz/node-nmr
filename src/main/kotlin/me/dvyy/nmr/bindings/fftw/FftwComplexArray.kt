package me.dvyy.nmr.bindings.fftw

import me.dvyy.nmr.bindings.helpers.Sizes
import me.dvyy.nmr.complex.ComplexDouble
import me.dvyy.nmr.complex.ComplexDoubleArray
import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * A native memory wrapper for `fftw_complex*` (a C array of `double[2]`).
 * Must be closed to free native memory.
 */
@JvmInline
value class FftwComplexArray private constructor(
    val segment: MemorySegment,
) {
    val size: Int get() = (segment.byteSize() / Sizes.COMPLEX).toInt()

    init {
        if (segment == MemorySegment.NULL) {
            throw OutOfMemoryError("fftw_malloc failed to allocate native memory.")
        }
    }

    /** Sets the real and imaginary parts at a specific index. */
    fun set(index: Int, real: Double, imag: Double) {
        require(index in 0 until size) { "Index out of bounds" }
        val offset = index * 16L
        segment.set(ValueLayout.JAVA_DOUBLE, offset, real)
        segment.set(ValueLayout.JAVA_DOUBLE, offset + 8L, imag)
    }

    /** Gets the complex number at a specific index. */
    fun get(index: Int): ComplexDouble {
        require(index in 0 until size) { "Index out of bounds" }
        val offset = index * 16L
        val real = segment.get(ValueLayout.JAVA_DOUBLE, offset)
        val imag = segment.get(ValueLayout.JAVA_DOUBLE, offset + 8L)
        return ComplexDouble(real, imag)
    }

    fun getReal(index: Int): Double = segment.get(ValueLayout.JAVA_DOUBLE, index * 16L)
    fun getImag(index: Int): Double = segment.get(ValueLayout.JAVA_DOUBLE, index * 16L + 8L)

    /** Populates the native memory using an interleaved double array (real, imag, real, imag...). */
    fun loadInterleaved(data: DoubleArray) {
//        require(data.size == size * 2) { "Data array must be exactly twice the size of the complex array." }
        MemorySegment.copy(data, 0, segment, ValueLayout.JAVA_DOUBLE, 0, data.size)
    }

    /** Extracts the native memory back into an interleaved JVM DoubleArray. */
    fun toInterleavedArray(): DoubleArray {
        val out = DoubleArray(size * 2)
        MemorySegment.copy(segment, ValueLayout.JAVA_DOUBLE, 0, out, 0, out.size)
        return out
    }

    fun copyFrom(other: FftwComplexArray) {
        if (this.size > other.size) segment.fill(0)
        segment.copyFrom(other.segment)
    }

    fun copyFromAsConjugate(other: FftwComplexArray) {
        if (this.size > other.size) segment.fill(0)
        for (i in 0 until other.size) {
            val real = other.getReal(i)
            val imag = other.getImag(i)
            set(i, real, -imag) // Complex conjugate
        }
    }

    companion object {
        context(arena: Arena)
        operator fun invoke(size: Int): FftwComplexArray {
            val byteSize = size * Sizes.COMPLEX
            return FftwComplexArray((FftwBindings.malloc.invokeExact(byteSize) as MemorySegment).reinterpret(byteSize, arena) {
                FftwBindings.free.invokeExact(it)
            })
        }

        fun fromSegment(segment: MemorySegment) = FftwComplexArray(segment)
    }
}

context(arena: Arena)
fun ComplexDoubleArray.toFFTWArray(): FftwComplexArray = FftwComplexArray(size).apply {
    loadInterleaved(this@toFFTWArray.data)
    return this
}