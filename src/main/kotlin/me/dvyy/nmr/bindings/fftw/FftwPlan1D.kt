package me.dvyy.nmr.bindings.fftw

import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment

/**
 * Represents a 1D FFTW Plan. 
 * Note: Creating the plan may overwrite `inArray` if the `FFTW_MEASURE` flag is used.
 */
class FftwPlan1D(
    val planPointer: MemorySegment,
) {
    init {
        if (planPointer == MemorySegment.NULL) {
            throw RuntimeException("Failed to create FFTW plan.")
        }
    }

    /** Executes the transform. */
    fun execute() {
        FftwBindings.execute.invokeExact(planPointer)
    }

    companion object {
        context(arena: Arena)
        operator fun invoke(
            size: Int,
            inArray: FftwComplexArray,
            outArray: FftwComplexArray,
            direction: FftwDirection,
            vararg flags: FftwFlag = arrayOf(FftwFlag.ESTIMATE)
        ): FftwPlan1D {
            val flattenedFlags = flags.fold(0) { acc, flag -> acc or flag.value }
            val planPointer: MemorySegment = (FftwBindings
                .planDft1d
                .invokeExact(size, inArray.segment, outArray.segment, direction.value, flattenedFlags) as MemorySegment)
                .reinterpret(1, arena) { FftwBindings.destroyPlan.invokeExact(it) }
            return FftwPlan1D(planPointer)
        }
    }
}