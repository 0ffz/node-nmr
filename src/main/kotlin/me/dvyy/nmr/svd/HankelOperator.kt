package me.dvyy.nmr.svd

import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.complex.asComplexInterweaved
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
//class HankelOperator(
//    val data: ComplexDoubleArray,
//) : ComplexLinearOperation {
//    val n: Int = ceil(data.size / 2.0).toInt()
//
//    override fun apply(input: DoubleArray, offset: Int): DoubleArray {
//        val input = input.asComplexInterweaved()
//        val offset = offset / 2
//        val result = ComplexDoubleArray(n)
//        val data = data
//        // we calculate column by column
//        for (i in 0 until n) {
//            val x = input[i + offset]
//            for (j in 0 until n) {
//                result[j] += data[i + j] * x
//            }
//        }
//        return result.data
//    }
//}

/**
 * Gets matrix representation of a linear operation by multiplying by identity on diagonals
 */
//fun ComplexLinearOperation.toMatrix(size: Int): List<ComplexDoubleArray> {
//    val matrix = Array(size) { ComplexDoubleArray(size) }.toList()
//    for (col in 0 until size) {
//        val input = DoubleArray(size * 2)
//        input[col * 2] = 1.0 // Real part of identity vector at col
//        val output = this.apply(input, 0)
//        val complexOutput = output.asComplexInterweaved()
//        for (row in 0 until size) {
//            matrix[row][col] = complexOutput[row]
//        }
//    }
//    return matrix
//}