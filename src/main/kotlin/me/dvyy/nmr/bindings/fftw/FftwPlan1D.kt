package me.dvyy.nmr.bindings.fftw

import java.lang.foreign.MemorySegment

/**
 * Represents a 1D FFTW Plan. 
 * Note: Creating the plan may overwrite `inArray` if the `FFTW_MEASURE` flag is used.
 */
class FftwPlan1D(
    val size: Int,
    inArray: MemorySegment,
    outArray: MemorySegment,
    direction: FftwDirection,
    flags: Int = FftwFlag.ESTIMATE.value
) : AutoCloseable {
    private val planPointer: MemorySegment = FftwBindings
        .planDft1d
        .invokeExact(size, inArray, outArray, direction.value, flags) as MemorySegment

    init {
        if (planPointer == MemorySegment.NULL) {
            throw RuntimeException("Failed to create FFTW plan.")
        }
    }

    /** Executes the transform. */
    fun execute() {
        FftwBindings.execute.invokeExact(planPointer)
    }

    override fun close() {
        if (planPointer != MemorySegment.NULL) {
            FftwBindings.destroyPlan.invokeExact(planPointer)
        }
    }
}