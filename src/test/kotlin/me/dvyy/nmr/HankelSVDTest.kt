package me.dvyy.nmr

import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.complex.asComplexInterweaved
import me.dvyy.nmr.svd.reconstructDiagonals
import org.junit.jupiter.api.Test


class HankelSVDTest {
    @Test
    fun ComplexDecompositionOfDiagonal() {
//        val myCustomMatrix = ComplexLinearOperation { x: DoubleArray, off: Int ->
//            val result = ComplexDoubleArray(5)
//            val x = x.asComplexInterweaved()
//            for (i in 0..4) {
//                result[i] = x[i + (off / 2)] * (i + 1)
//            }
//            result.data
//        }
//        val solver = MatrixDecomposition.eigshComplex(myCustomMatrix, 5, 3, "LM", null, 100, 1e-5)
//        solver.solve()
//        val solved = ComplexEigenDecomposition.of(solver.eigenvalues, solver.eigenvectors)
//        println(solved)
    }

    @Test
    fun `diagonal reduction`() {
//        val solution = ComplexEigenDecomposition.of(doubleArrayOf(2.0, 0.0, 3.0, 0.0), doubleArrayOf(1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0))
//        println(solution.reconstructDiagonals())
    }
}