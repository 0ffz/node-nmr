package me.dvyy.nmr

import io.kotest.matchers.doubles.plusOrMinus
import io.kotest.matchers.shouldBe
import me.dvyy.nmr.bindings.helpers.memScoped
import me.dvyy.nmr.bindings.propack.Propack
import me.dvyy.nmr.complex.takeComplex
import me.dvyy.nmr.parsing.BrukerDataset
import me.dvyy.nmr.svd.HankelOperator
import org.junit.jupiter.api.Test
import kotlin.time.measureTimedValue

class CadzowFilteringTests {
    @Test
    fun `diagonal matrix should return diagonals as its singular values`() {
        // arrange
        val rows = 10
        val cols = 10
        val numWanted = 10

        // act
        val result = memScoped {
            Propack.partialComplexSVD(diagonalMatrixOperator, rows, cols, numWanted)
        }

        // assert
        result.singularValues.size shouldBe numWanted
        for (i in 0 until numWanted) {
            val expected = (numWanted - i).toDouble()
            result.singularValues[i] shouldBe (expected plusOrMinus 1e-6)
        }
    }

    @Test
    fun `should get correct number of singular values on real data`() {
        // arrange
        val datasetPath = "data/1d_carbon_ML/8"
        val data = BrukerDataset(datasetPath)
        val fid = data.readFid().takeComplex(2048)
        val rows = fid.size / 2
        val cols = fid.size - rows + 1
        val numWanted = 15

        // act
        val (result, time) = measureTimedValue {
            memScoped {
                Propack.partialComplexSVD(
                    HankelOperator(this, fid.toMemorySegment(), rows, cols),
                    rows, cols,
                    numWanted = numWanted
                )
            }
        }
        println("Took $time")

        // assert
        result.singularValues.size shouldBe numWanted
    }
}