package me.dvyy.nmr.bindings.fftw

import org.scijava.nativelib.NativeLoader
import java.lang.foreign.FunctionDescriptor
import java.lang.foreign.Linker
import java.lang.foreign.SymbolLookup
import java.lang.foreign.ValueLayout
import java.lang.invoke.MethodHandle

internal object FftwBindings {
    init {
        NativeLoader.loadLibrary("fftw3")
    }

    private val linker = Linker.nativeLinker()

    // Loads the system-installed fftw3 library. 
    // Ensure "fftw3.dll" (Win), "libfftw3.so" (Linux), or "libfftw3.dylib" (Mac) is in your library path.
//    private val fftwLib = SymbolLookup.libraryLookup("fftw3", Arena.global())
    val fftwLib = SymbolLookup.loaderLookup()

    private fun lookup(name: String) = fftwLib.find(name)
        .orElseThrow { UnsatisfiedLinkError("Could not find FFTW function: $name") }

    val malloc: MethodHandle = linker.downcallHandle(
        lookup("fftw_malloc"),
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)
    )

    val free: MethodHandle = linker.downcallHandle(
        lookup("fftw_free"),
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    )

    val planDft1d: MethodHandle = linker.downcallHandle(
        lookup("fftw_plan_dft_1d"),
        FunctionDescriptor.of(
            ValueLayout.ADDRESS, // Returns: fftw_plan (pointer)
            ValueLayout.JAVA_INT, // n (size)
            ValueLayout.ADDRESS, // in array (pointer)
            ValueLayout.ADDRESS, // out array (pointer)
            ValueLayout.JAVA_INT, // sign
            ValueLayout.JAVA_INT  // flags
        )
    )

    val execute: MethodHandle = linker.downcallHandle(
        lookup("fftw_execute"),
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    )

    val destroyPlan: MethodHandle = linker.downcallHandle(
        lookup("fftw_destroy_plan"),
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    )
}
