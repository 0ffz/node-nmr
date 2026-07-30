package me.dvyy.nmr.bindings.propack

import me.dvyy.nmr.common.math.ComplexDouble
import me.dvyy.nmr.common.math.ComplexDoubleArray
import me.dvyy.nmr.common.math.ComplexDoubleMatrix
import java.lang.foreign.Arena
import java.lang.foreign.ValueLayout.JAVA_DOUBLE
import java.lang.foreign.ValueLayout.JAVA_INT
import kotlin.math.min

object Propack {
    context(arena: Arena)
    fun partialComplexSVD(
        operator: LinearOperator,
        rows: Int,
        cols: Int,
        numWanted: Int,
    ): SVDResult {
        val maxPossible = min(rows, cols)
        val actualNumWanted = numWanted.coerceIn(1, maxPossible)

        // Determine Krylov subspace dimension: bounded by maxPossible
        val dim = min(maxPossible, maxOf(actualNumWanted * 2 + 1, actualNumWanted + 10, 20))

        // Calculate shifts per restart P ensuring kWanted <= dim - P - 1 when restarts are used
        val shiftsPerRestart = if (dim > actualNumWanted) {
            maxOf(1, dim - actualNumWanted - 1)
        } else {
            0
        }

        val kWanted = if (shiftsPerRestart > 0) {
            actualNumWanted.coerceAtMost(dim - shiftsPerRestart - 1)
        } else {
            actualNumWanted
        }

        // Allocate Matrix Arrays (Sizes based on PROPACK documentation)
        val uMatrix = arena.allocate(JAVA_DOUBLE, (rows * (dim + 1) * 2L)) // *2 for complex
        val vMatrix = arena.allocate(JAVA_DOUBLE, (cols * dim * 2L))
        val sigmaValues = arena.allocate(JAVA_DOUBLE, kWanted.toLong())
        val errorBounds = arena.allocate(JAVA_DOUBLE, kWanted.toLong())

        // Allocate Work Arrays
        // A standard block size for BLAS-3 performance.
        // You can increase this to 64 if you have plenty of RAM.
        val nb = 32

        // Formula from docs, given computeU and computeV are both true:
        // M + N + 10*DIM + 5*DIM**2 + 4 + MAX(3*DIM**2+4*DIM+4, NB*MAX(M,N))
        val dimSq = dim * dim
        val maxMN = maxOf(rows, cols)
        val workSize = rows + cols +
                (10 * dim) +
                (5 * dimSq) + 4 +
                maxOf(3 * dimSq + 4 * dim + 4, nb * maxMN)
        val work = arena.allocate(JAVA_DOUBLE, workSize.toLong())

        // Formula: M + N + NB*MAX(M,N)
        val zWorkSize = rows + cols + (nb * maxMN)
        val zWork = arena.allocate(JAVA_DOUBLE, zWorkSize * 2L)

        // Formula: 8*DIM
        val iWorkSize = 8 * dim
        val iWork = arena.allocate(JAVA_INT, iWorkSize.toLong())

        // Options arrays
        val dOption = arena.allocate(JAVA_DOUBLE, 4L)
        val iOption = arena.allocate(JAVA_INT, 2L)

        // Dummy parameters
        val zParm = arena.allocate(JAVA_DOUBLE, 2L)
        val iParm = arena.allocate(JAVA_INT, 1L)

        // Execute the Lanczos SVD
        val info = PropackBindings.zlansvdIrl(
            arena = arena,
            target = SingularTripletTarget.LARGEST,
            computeU = ComputeVectors.YES,
            computeV = ComputeVectors.YES,
            mRows = rows,
            nCols = cols,
            dim = dim,
            shiftsPerRestart = shiftsPerRestart,
            numWanted = kWanted,
            maxRestarts = 1000,
            aprod = operator,
            uMatrix = uMatrix,
            ldu = rows,
            sigmaValues = sigmaValues,
            errorBounds = errorBounds,
            vMatrix = vMatrix,
            ldv = cols,
            tolerance = 1e-12,
            work = work,
            workSize = workSize,
            zWork = zWork,
            zWorkSize = zWorkSize,
            iWork = iWork,
            iWorkSize = iWorkSize,
            dOption = dOption,
            iOption = iOption,
            zParm = zParm,
            iParm = iParm
        )

        if (info == 0) {
            val singularValues = DoubleArray(kWanted) { i ->
                sigmaValues.get(JAVA_DOUBLE, i * 8L)
            }

            val u = ComplexDoubleMatrix(
                Array(kWanted) { col ->
                    val arr = ComplexDoubleArray(rows)
                    for (row in 0 until rows) {
                        val offset = (col * rows + row) * 16L
                        arr[row] = ComplexDouble(
                            uMatrix[JAVA_DOUBLE, offset],
                            uMatrix[JAVA_DOUBLE, offset + 8L]
                        )
                    }
                    arr
                }
            )

            val v = ComplexDoubleMatrix(
                Array(kWanted) { col ->
                    val arr = ComplexDoubleArray(cols)
                    for (row in 0 until cols) {
                        val offset = (col * cols + row) * 16L
                        arr[row] = ComplexDouble(
                            vMatrix[JAVA_DOUBLE, offset],
                            vMatrix[JAVA_DOUBLE, offset + 8L]
                        )
                    }
                    arr
                }
            )

            return SVDResult(rows, cols, u, singularValues, v)
        } else {
            error("Only ${-info} singular triplets converge before exceeding MAXITER iterations.")
        }
    }
}
