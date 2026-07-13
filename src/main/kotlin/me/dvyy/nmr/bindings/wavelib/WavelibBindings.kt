package me.dvyy.nmr.bindings.wavelib

import org.scijava.nativelib.NativeLoader
import java.lang.foreign.*
import java.lang.invoke.MethodHandle
import java.util.function.Supplier


object WavelibBindings {
    init {
        NativeLoader.loadLibrary("wavelib")
    }

    private val LINKER: Linker = Linker.nativeLinker()

    // Assuming the wavelib shared library (.so, .dll, or .dylib) is on your library path
    private val LIB: SymbolLookup = SymbolLookup.loaderLookup()

    // Function Handles
    val WAVE_INIT: MethodHandle = downcall(
        "wave_init",
        FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    )

    val WT_INIT: MethodHandle = downcall(
        "wt_init",
        FunctionDescriptor.of(
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
            ValueLayout.ADDRESS,
            ValueLayout.JAVA_INT,
            ValueLayout.JAVA_INT
        )
    )

    val SET_WT_CONV: MethodHandle = downcall(
        "setWTConv",
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    )

    val SWT: MethodHandle = downcall(
        "swt",
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    )

    val ISWT: MethodHandle = downcall(
        "iswt",
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.ADDRESS)
    )

    val WT_FREE: MethodHandle = downcall(
        "wt_free",
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    )

    val WAVE_FREE: MethodHandle = downcall(
        "wave_free",
        FunctionDescriptor.ofVoid(ValueLayout.ADDRESS)
    )

    // Struct Mapping for wt_set to access wt->output and wt->outlength
    val WT_SET_LAYOUT: StructLayout = MemoryLayout.structLayout(
        ValueLayout.ADDRESS.withName("wave"),                             // Offset: 0
        ValueLayout.ADDRESS.withName("cobj"),                             // Offset: 8
        MemoryLayout.sequenceLayout(10, ValueLayout.JAVA_BYTE).withName("method"), // Offset: 16
        MemoryLayout.paddingLayout(2),                                    // Offset: 26 -> Pad to 28 for int alignment
        ValueLayout.JAVA_INT.withName("siglength"),                       // Offset: 28
        ValueLayout.JAVA_INT.withName("modwtsiglength"),                  // Offset: 32
        ValueLayout.JAVA_INT.withName("outlength"),                       // Offset: 36
        ValueLayout.JAVA_INT.withName("lenlength"),                       // Offset: 40
        ValueLayout.JAVA_INT.withName("J"),                               // Offset: 44
        ValueLayout.JAVA_INT.withName("MaxIter"),                         // Offset: 48
        ValueLayout.JAVA_INT.withName("even"),                            // Offset: 52
        MemoryLayout.sequenceLayout(10, ValueLayout.JAVA_BYTE).withName("ext"),    // Offset: 56
        MemoryLayout.sequenceLayout(10, ValueLayout.JAVA_BYTE).withName("cmethod"), // Offset: 66
        // Current offset is 76. This is naturally a multiple of 4, so no padding needed for the next int.
        ValueLayout.JAVA_INT.withName("N"),                               // Offset: 76
        ValueLayout.JAVA_INT.withName("cfftset"),                         // Offset: 80
        ValueLayout.JAVA_INT.withName("zpad"),                            // Offset: 84
        MemoryLayout.sequenceLayout(102, ValueLayout.JAVA_INT).withName("length"), // Offset: 88
        // Current offset is 496. This is naturally a multiple of 8, so no padding needed for the pointer.
        ValueLayout.ADDRESS.withName("output")                            // Offset: 496
        // Ignoring flexible array `params[0]` as we only need to map up to `output`.
    );

    val OUTLENGTH_OFFSET: Long = WT_SET_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("outlength"))
    val OUTPUT_OFFSET: Long = WT_SET_LAYOUT.byteOffset(MemoryLayout.PathElement.groupElement("output"))

    private fun downcall(name: String?, desc: FunctionDescriptor?): MethodHandle {
        return LINKER.downcallHandle(
            LIB.find(name).orElseThrow(Supplier { UnsatisfiedLinkError("Cannot find symbol: " + name) }),
            desc
        )
    }
}
