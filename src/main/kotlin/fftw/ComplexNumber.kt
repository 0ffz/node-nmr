package fftw

import me.dvyy.nmr.complex.ComplexDoubleArray
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

data class ComplexNumber(val real: Double, val imag: Double) {
}

/**
 * A native memory wrapper for `fftw_complex*` (a C array of `double[2]`).
 * Must be closed to free native memory.
 */
class FftwComplexArray(val size: Int) : AutoCloseable {
    // 2 doubles (8 bytes each) = 16 bytes per complex number
    private val byteSize = size * 16L

    val segment: MemorySegment = (FftwBindings.malloc.invokeExact(byteSize) as MemorySegment).reinterpret(byteSize)

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
    fun get(index: Int): ComplexNumber {
        require(index in 0 until size) { "Index out of bounds" }
        val offset = index * 16L
        val real = segment.get(ValueLayout.JAVA_DOUBLE, offset)
        val imag = segment.get(ValueLayout.JAVA_DOUBLE, offset + 8L)
        return ComplexNumber(real, imag)
    }

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

    override fun close() {
        if (segment != MemorySegment.NULL) {
            FftwBindings.free.invokeExact(segment)
        }
    }

    companion object {
        //FIXME context arena
        fun ComplexDoubleArray.toFFTWArray(): FftwComplexArray = FftwComplexArray(size).apply {
            loadInterleaved(this@toFFTWArray.data)
            return this
        }
    }
}