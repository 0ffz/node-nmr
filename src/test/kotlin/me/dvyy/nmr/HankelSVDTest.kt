package me.dvyy.nmr

import me.dvyy.nmr.bindings.helpers.memScoped
import me.dvyy.nmr.bindings.propack.Propack
import me.dvyy.nmr.parsing.BrukerDataset
import me.dvyy.nmr.svd.HankelOperator
import me.dvyy.nmr.svd.HankelOperatorBruteForce
import me.dvyy.nmr.ui.nodes.transformations.zeroFill
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.lang.foreign.MemorySegment
import java.lang.foreign.ValueLayout.*
import kotlin.time.measureTime


import me.dvyy.nmr.svd.reconstructDiagonals
import org.junit.jupiter.api.Assertions.assertTrue

class HankelSVDTest {
    @Test
    fun `reconstructDiagonals returns valid non-NaN array`() {
        val rows = 20
        val cols = 20
        val targetLen = rows + cols - 1
        val random = java.util.Random(42)
        val data = DoubleArray(targetLen * 2) { random.nextDouble() }

        memScoped {
            val hankelDataSeg = this.allocate(JAVA_DOUBLE, targetLen * 2L)
            MemorySegment.copy(data, 0, hankelDataSeg, JAVA_DOUBLE, 0, data.size)
            val fftOp = HankelOperator(this, hankelDataSeg, rows, cols)
            val result = Propack.partialComplexSVD(fftOp, rows, cols, numWanted = 5)
            val reconstructed = result.reconstructDiagonals()

            assertEquals(targetLen, reconstructed.size)
            for (i in 0 until reconstructed.size) {
                val elem = reconstructed[i]
                assertTrue(!elem.re.isNaN() && !elem.im.isNaN(), "Element at index $i is NaN")
            }
        }
    }

    @Test
    fun ComplexDecompositionOfDiagonal() {
        val result = memScoped {
            Propack.partialComplexSVD(diagonalMatrixOperator, 10, 10, 10)
        }
        println(result.singularValues.toList())
    }

    @Test
    fun `svd on real data`() {
        val data = BrukerDataset("data/1d_carbon_ML/8")
        val fid = data.readFid()
        val rows = fid.size / 2
        val cols = fid.size - rows + 1
        val time = measureTime {
            val result = memScoped {
                Propack.partialComplexSVD(
                    HankelOperator(this, fid.toMemorySegment(), rows, cols),
                    rows, cols,
                    numWanted = 15
                )
            }
            println(result.singularValues.toList())
        }
        println("Took $time")
    }

    @Test
    fun `HankelOperator correctness check`() {
        // Test with different combinations of dimensions
        val testCases = listOf(
            Pair(15, 5),   // rows > cols
            Pair(5, 15),   // rows < cols
            Pair(10, 10)   // rows == cols
        )

        for ((rows, cols) in testCases) {
            val targetLen = rows + cols - 1

            // Create random test data
            val random = java.util.Random(42)
            val data = DoubleArray(targetLen * 2) { random.nextDouble() }

            memScoped {
                val hankelDataSeg = this.allocate(JAVA_DOUBLE, targetLen * 2L)
                MemorySegment.copy(data, 0, hankelDataSeg, JAVA_DOUBLE, 0, data.size)

                val bruteOp = HankelOperatorBruteForce(hankelDataSeg)
                val fftOp = HankelOperator(this, hankelDataSeg, rows, cols)

                // 1. Test normal multiply (input length is cols, output length is rows)
                val inputLength = cols
                val outputLength = rows
                val xData = DoubleArray(inputLength * 2) { random.nextDouble() }
                val xSeg = this.allocate(JAVA_DOUBLE, inputLength * 2L)
                MemorySegment.copy(xData, 0, xSeg, JAVA_DOUBLE, 0, xData.size)

                val yBrute = this.allocate(JAVA_DOUBLE, outputLength * 2L)
                val yFft = this.allocate(JAVA_DOUBLE, outputLength * 2L)

                bruteOp.multiply(false, inputLength, outputLength, xSeg, yBrute, MemorySegment.NULL, MemorySegment.NULL)
                fftOp.multiply(false, inputLength, outputLength, xSeg, yFft, MemorySegment.NULL, MemorySegment.NULL)

                for (i in 0 until outputLength * 2) {
                    val expected = yBrute.get(JAVA_DOUBLE, i * 8L)
                    val actual = yFft.get(JAVA_DOUBLE, i * 8L)
                    assertEquals(expected, actual, 1e-9, "Mismatch at index $i for normal multiply with rows=$rows, cols=$cols")
                }

                // 2. Test transpose multiply (input length is rows, output length is cols)
                val xDataTrans = DoubleArray(outputLength * 2) { random.nextDouble() }
                val xSegTrans = this.allocate(JAVA_DOUBLE, outputLength * 2L)
                MemorySegment.copy(xDataTrans, 0, xSegTrans, JAVA_DOUBLE, 0, xDataTrans.size)

                val yBruteTrans = this.allocate(JAVA_DOUBLE, inputLength * 2L)
                val yFftTrans = this.allocate(JAVA_DOUBLE, inputLength * 2L)

                bruteOp.multiply(true, outputLength, inputLength, xSegTrans, yBruteTrans, MemorySegment.NULL, MemorySegment.NULL)
                fftOp.multiply(true, outputLength, inputLength, xSegTrans, yFftTrans, MemorySegment.NULL, MemorySegment.NULL)

                for (i in 0 until inputLength * 2) {
                    val expected = yBruteTrans.get(JAVA_DOUBLE, i * 8L)
                    val actual = yFftTrans.get(JAVA_DOUBLE, i * 8L)
                    assertEquals(expected, actual, 1e-9, "Mismatch at index $i for transpose multiply with rows=$rows, cols=$cols")
                }
            }
        }
    }
}