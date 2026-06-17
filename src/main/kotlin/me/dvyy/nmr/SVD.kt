package me.dvyy.nmr

import net.scoreworks.arpackj.LinearOperation
import net.scoreworks.arpackj.eig.MatrixDecomposition
import org.jetbrains.bio.viktor.plus
import org.jetbrains.kotlinx.multik.api.arange
import org.jetbrains.kotlinx.multik.api.diagonal
import org.jetbrains.kotlinx.multik.api.linalg.dot
import org.jetbrains.kotlinx.multik.api.mk
import org.jetbrains.kotlinx.multik.api.zeros
import org.jetbrains.kotlinx.multik.api.ndarray
import org.jetbrains.kotlinx.multik.ndarray.complex.plus
import org.jetbrains.kotlinx.multik.ndarray.data.get
import org.jetbrains.kotlinx.multik.ndarray.data.set
import org.jetbrains.kotlinx.multik.ndarray.operations.plus
import org.jetbrains.kotlinx.multik.ndarray.operations.plusAssign
import org.jetbrains.kotlinx.multik.ndarray.operations.times
import org.jetbrains.kotlinx.multik.ndarray.operations.toDoubleArray
import space.kscience.kmath.nd.Floa64FieldOpsND.Companion.plus
import space.kscience.kmath.operations.Float64Field.plus
import kotlin.math.ceil

/**
 * Suppose [data] is [a, b, c, d, e, f, g]
 *
 * We want a symmetric n x n matrix, so take `n = ceil(size / 2) = ceil(7 / 2) = 4` and stack as follows:
 *
 * [ a, b, c, d ]     [ x ]
 * [ b, c, d, e ]  *  [ y ]
 * [ c, d, e, f ]     [ z ]
 * [ d, e, f, g ]     [ w ]
 *
 * Where [apply] takes a vector [x, y, z, w]:
 * [ a*x + b*y + c*z + d*w ]
 * [ b*x + c*y + d*z + e*w ]
 * [ c*x + d*y + e*z + f*w ]
 * [ d*x + e*y + f*z + g*w ]
 *
 * so `output[i] = row(i) dot x`
 */
class HankelMatrixLinearOperation(
    val data: DoubleArray,
) : LinearOperation {
    val n: Int = ceil(data.size / 2.0).toInt()

    override fun apply(input: DoubleArray, offset: Int): DoubleArray {
        val result = DoubleArray(n)
        // we calculate column by column
        for (i in 0 until n) {
            val x = input[i + offset]
            for (j in 0 until n) {
                result[j] += data[i + j] * x
            }
        }
        return result
    }

}

fun hankelSVD(originalData: DoubleArray, k: Int): DoubleArray {
//    val originalData = mk.arange<Double>(1000).toDoubleArray()
    val hankel = HankelMatrixLinearOperation(originalData)
    val size = hankel.n
//    val size = 5
//    val hankel = LinearOperation { x: DoubleArray, off: Int ->
//        val result = DoubleArray(size)
//        for (i in 0 until size) {
//            result[i] = x[i + off] * (i + 1)
//        }
//        result
//    }
    val solver = MatrixDecomposition.eigsh(hankel, size, k, "LM", null, 100, 1e-5)
    // 1
    // 2
    // 3
    // 4
    solver.solve()
    val eigenvectors = mk.ndarray(solver.eigenvectors, k, size)
    val eigenvalues = solver.eigenvalues
    println("Eigenvalues: ${eigenvalues.toList()}")

    val reconstructed = mk.zeros<Double>(size, size)
    for (i in 0 until k) {
        val lambda = eigenvalues[i]

        for (r in 0 until size) {
            // Cache the scaled row value to avoid recalculating it in the inner loop
            val scaledVr = eigenvectors[i, r] * lambda

            for (c in 0 until size) {
                // The inner loop iterates over 'c' (columns).
                // Assuming row-major memory layout, this guarantees cache-friendly,
                // contiguous memory access for both the read and the write.
                reconstructed[r, c] += scaledVr * eigenvectors[i, c]
            }
        }
    }

    // sum on anti-diagonals
    // [ 1, 2, 3 ]
    // [ 4, 5, 6 ]
    // [ 7, 8, 9 ]
    // => [1, 4 + 2, 7 + 5 + 3, 8 + 6, 9]
    val reconstructedArray = DoubleArray(originalData.size)
    val counts = IntArray(originalData.size)
    for (r in 0 until size) {
        for (c in 0 until size) {
            val idx = r + c
            if (idx < reconstructedArray.size) {
                reconstructedArray[idx] += reconstructed[r, c]
                counts[idx]++
            }
        }
    }
    for (i in reconstructedArray.indices) {
        if (counts[i] > 0) reconstructedArray[i] /= counts[i].toDouble()
    }
//    println("Reconstructed Data: ${reconstructedArray.toList()}")
    return reconstructedArray
//    val reconstructed =
//    Vector.zeros(ScalarType.Float64, hankel.n, k)
//    val inverse = eigenvectors
//    println(eigenvectors)
//    println(inverse)
//    println(solver.eigenvalues.toList())
//    println(solver.eigenvectors.toList())
}