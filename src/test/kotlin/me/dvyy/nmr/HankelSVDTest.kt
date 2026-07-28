package me.dvyy.nmr

import me.dvyy.nmr.bindings.helpers.memScoped
import me.dvyy.nmr.bindings.propack.Propack
import me.dvyy.nmr.parsing.BrukerDataset
import me.dvyy.nmr.svd.HankelOperator
import me.dvyy.nmr.ui.nodes.transformations.zeroFill
import org.junit.jupiter.api.Test
import kotlin.time.measureTime


class HankelSVDTest {
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
                    numWanted = 80
                )
            }
            println(result.singularValues.toList())
        }
        println("Took $time")
    }
}