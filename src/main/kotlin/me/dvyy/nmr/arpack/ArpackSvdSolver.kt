package me.dvyy.nmr.arpack

import java.lang.foreign.Arena
import java.lang.foreign.ValueLayout

class ArpackSvdSolver(private val bindings: ArpackBindings) {

    fun solveTopK(operator: NativeLinearOperator, k: Int): DoubleArray {
        val n = operator.cols
        val ncv = Math.min(n, 2 * k + 1) // Rule of thumb for Arnoldi vectors

        // Open a confined arena for native allocations tied to this method call
        Arena.ofConfined().use { arena ->
            // Allocate scalar parameters (Fortran needs pointers to these)
            val ido = arena.allocateFrom(ValueLayout.JAVA_INT, 0)
            val nPtr = arena.allocateFrom(ValueLayout.JAVA_INT, n)
            val nev = arena.allocateFrom(ValueLayout.JAVA_INT, k)
            val tol = arena.allocateFrom(ValueLayout.JAVA_DOUBLE, 0.0) // Machine precision
            val info = arena.allocateFrom(ValueLayout.JAVA_INT, 0)

            // Allocate string parameters (e.g., "I" for standard eigenvalue problem)
            val bmat = arena.allocateFrom("I")
            val which = arena.allocateFrom("LM") // Largest Magnitude eigenvalues

            // Allocate workspace arrays
            val resid = arena.allocate(ValueLayout.JAVA_DOUBLE, n.toLong())
            val v = arena.allocate(ValueLayout.JAVA_DOUBLE, (n * ncv).toLong())
            val workd = arena.allocate(ValueLayout.JAVA_DOUBLE, (3 * n).toLong())
            val workl = arena.allocate(ValueLayout.JAVA_DOUBLE, (ncv * (ncv + 8)).toLong())

            val ncvPtr = arena.allocateFrom(ValueLayout.JAVA_INT, ncv)
            val ldv = arena.allocateFrom(ValueLayout.JAVA_INT, n)
            val lworkl = arena.allocateFrom(ValueLayout.JAVA_INT, ncv * (ncv + 8))
            val iparam = arena.allocate(ValueLayout.JAVA_INT, 11)
            iparam.setAtIndex(ValueLayout.JAVA_INT, 0, 1) // ishift = 1
            iparam.setAtIndex(ValueLayout.JAVA_INT, 2, 300) // mxiter = 300
            val ipntr = arena.allocate(ValueLayout.JAVA_INT, 11)

            // The Reverse Communication Loop
            while (true) {
                // Call native dsaupd. Use invoke() and cast segments.
                bindings.dsaupd.invoke(
                    ido, bmat, nPtr, which, nev, tol, resid, ncvPtr,
                    v, ldv, iparam, ipntr, workd, workl, lworkl, info
                )

                val currentIdo = ido.get(ValueLayout.JAVA_INT, 0)

                if (currentIdo == -1 || currentIdo == 1) {
                    // ARPACK wants us to compute Y = A^T * A * X
                    // Extract the pointers from IPNTR (Fortran is 1-indexed, so subtract 1 for FFM byte offsets)
                    val ipntrX = ipntr.getAtIndex(ValueLayout.JAVA_INT, 0) - 1
                    val ipntrY = ipntr.getAtIndex(ValueLayout.JAVA_INT, 1) - 1

                    // Slice the WORKD memory segment to act as independent arrays
                    val xSegment = workd.asSlice((ipntrX * 8).toLong(), (n * 8).toLong())
                    val ySegment = workd.asSlice((ipntrY * 8).toLong(), (n * 8).toLong())

                    // Execute the custom Kotlin logic zero-copy
                    operator.applyAtA(xSegment, ySegment)
                }
                else if (currentIdo == 99) {
                    break // Convergence achieved!
                }
                else {
                    throw RuntimeException("ARPACK error. IDO = $currentIdo")
                }
            }

            // After IDO == 99, you would call dseupd (similar FFM setup)
            // to extract the eigenvalues, take their square roots, and return them.
            return TODO() //extractSingularValuesFromDseupd(arena, ...)
        }
    }
}