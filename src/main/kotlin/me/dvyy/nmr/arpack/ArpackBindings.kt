package me.dvyy.nmr.arpack

import java.lang.foreign.*
import java.lang.invoke.MethodHandle

class ArpackBindings {
    private val linker = Linker.nativeLinker()
    
    // Assumes libarpack.so (Linux), libarpack.dylib (Mac), or arpack.dll (Windows) is in the library path
    private val arpackLib = SymbolLookup.libraryLookup("arpack.so", Arena.global())

    val dsaupd: MethodHandle = linker.downcallHandle(
        arpackLib.find("dsaupd_").orElseThrow { IllegalStateException("dsaupd not found") },
        FunctionDescriptor.ofVoid(
            ValueLayout.ADDRESS, // ido
            ValueLayout.ADDRESS, // bmat
            ValueLayout.ADDRESS, // n
            ValueLayout.ADDRESS, // which
            ValueLayout.ADDRESS, // nev
            ValueLayout.ADDRESS, // tol
            ValueLayout.ADDRESS, // resid
            ValueLayout.ADDRESS, // ncv
            ValueLayout.ADDRESS, // v
            ValueLayout.ADDRESS, // ldv
            ValueLayout.ADDRESS, // iparam
            ValueLayout.ADDRESS, // ipntr
            ValueLayout.ADDRESS, // workd
            ValueLayout.ADDRESS, // workl
            ValueLayout.ADDRESS, // lworkl
            ValueLayout.ADDRESS  // info
        )
    )
}

fun main() {
    ArpackBindings().dsaupd
}