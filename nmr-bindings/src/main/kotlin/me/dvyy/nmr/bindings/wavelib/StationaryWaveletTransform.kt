package me.dvyy.nmr.bindings.wavelib

import java.lang.foreign.Arena
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout

/**
 * Idiomatic wrapper for performing 1D Stationary Wavelet Transforms (SWT) using wavelib.
 *
 * @param waveletName The name of the wavelet (e.g., "bior3.5", "db2")
 * @param signalLength The length of the input signal (N). Must be even/power of 2 depending on wavelet.
 * @param level The decomposition level (J).
 */
class StationaryWaveletTransform(
    waveletName: String = "bior3.5",
    val signalLength: Int,
    val level: Int = 1,
) : AutoCloseable {

    // Tie native allocations to this arena. It is closed when the wrapper is closed.
    private val arena = Arena.ofShared()

    private val waveObj: MemorySegment
    private val wtObj: MemorySegment

    init {
        try {
            // Allocate C-strings
            val nameStr = arena.allocateFrom(waveletName)
            val swtStr = arena.allocateFrom("swt")
            val directStr = arena.allocateFrom("direct")

            // wave_object obj = wave_init(name);
            waveObj = WavelibBindings.WAVE_INIT.invokeExact(nameStr) as MemorySegment

            // wt_object wt = wt_init(obj, "swt", N, J);
            wtObj = (WavelibBindings.WT_INIT.invokeExact(waveObj, swtStr, signalLength, level) as MemorySegment).reinterpret(WavelibBindings.WT_SET_LAYOUT.byteSize())

            // setWTConv(wt, "direct");
            WavelibBindings.SET_WT_CONV.invokeExact(wtObj, directStr)

        } catch (e: Throwable) {
            arena.close()
            throw RuntimeException("Failed to initialize wavelib objects", e)
        }
    }

    /**
     * Performs the Forward SWT.
     * @return DoubleArray containing approximation and detail coefficients.
     */
    fun forward(input: DoubleArray): DoubleArray {
        require(input.size == signalLength) {
            "Input size (${input.size}) must match initialized signal length ($signalLength)."
        }

        // Allocate native array and copy Java array into it
        val inpSegment = arena.allocateFrom(ValueLayout.JAVA_DOUBLE, *input)

        // swt(wt, inp);
        WavelibBindings.SWT.invokeExact(wtObj, inpSegment)

        // Read wt->outlength and wt->output pointers directly from the struct
        val outLength = wtObj.get(ValueLayout.JAVA_INT, WavelibBindings.OUTLENGTH_OFFSET)
        val outputPtr = wtObj.get(ValueLayout.ADDRESS, WavelibBindings.OUTPUT_OFFSET)

        // Reinterpret the opaque output pointer as an array of known length
        val outputSegment = outputPtr.reinterpret((outLength * ValueLayout.JAVA_DOUBLE.byteSize()).toLong())

        // Copy back to a safe Kotlin array
        return outputSegment.toArray(ValueLayout.JAVA_DOUBLE)
    }

    /**
     * Performs the Inverse SWT (ISWT).
     * @return The reconstructed signal of size `signalLength`.
     */
    fun inverse(): DoubleArray {
        // Allocate space for the output
        val outSegment = arena.allocate(ValueLayout.JAVA_DOUBLE, signalLength.toLong())

        // iswt(wt, out);
        WavelibBindings.ISWT.invokeExact(wtObj, outSegment)

        return outSegment.toArray(ValueLayout.JAVA_DOUBLE)
    }

    /**
     * Performs the Inverse SWT (ISWT).
     * @param modifiedCoefficients Optional modified coefficients for denoising.
     * If provided, they overwrite the native memory state before ISWT.
     * @return The reconstructed signal of size `signalLength`.
     */
    fun inverse(modifiedCoefficients: DoubleArray? = null): DoubleArray {
        if (modifiedCoefficients != null) {
            val outLength = wtObj.get(ValueLayout.JAVA_INT, WavelibBindings.OUTLENGTH_OFFSET)
            require(modifiedCoefficients.size == outLength) {
                "Modified coefficients size (${modifiedCoefficients.size}) must match outLength ($outLength)."
            }

            // 1. Get the native pointer to wt->output and reinterpret its bounds
            val outputPtr = wtObj.get(ValueLayout.ADDRESS, WavelibBindings.OUTPUT_OFFSET)
            val outputSegment = outputPtr.reinterpret((outLength * ValueLayout.JAVA_DOUBLE.byteSize()).toLong())

            // 2. Allocate a temporary native segment for the user's array
            val tempSegment = arena.allocateFrom(ValueLayout.JAVA_DOUBLE, *modifiedCoefficients)

            // 3. Copy the modified coefficients back into the wavelib struct memory
            MemorySegment.copy(tempSegment, 0, outputSegment, 0, tempSegment.byteSize())
        }

        // Allocate space for the output reconstruction
        val outSegment = arena.allocate(ValueLayout.JAVA_DOUBLE, signalLength.toLong())

        // iswt(wt, out);
        WavelibBindings.ISWT.invokeExact(wtObj, outSegment)

        return outSegment.toArray(ValueLayout.JAVA_DOUBLE)
    }

    /**
     * Safely frees native memory and wavelib structs.
     */
    override fun close() {
        try {
            WavelibBindings.WT_FREE.invokeExact(wtObj)
            WavelibBindings.WAVE_FREE.invokeExact(waveObj)
        } finally {
            arena.close()
        }
    }
}
