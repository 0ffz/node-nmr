package me.dvyy.nmr

import me.dvyy.nmr.complex.ComplexDouble
import me.dvyy.nmr.complex.complexDoubleArrayOf
import me.dvyy.nmr.svd.HankelOperator
import me.dvyy.nmr.svd.hankelSVD
import me.dvyy.nmr.svd.toMatrix
import org.junit.jupiter.api.Test

class HankelOperatorTest {
    @Test
    fun `should correctly construct hankel matrix when multiplying identity`() {
        val operator = HankelOperator(
            complexDoubleArrayOf(
                ComplexDouble(1),
                ComplexDouble(2),
                ComplexDouble(3),
                ComplexDouble(4),
                ComplexDouble(5),
                ComplexDouble(6),
                ComplexDouble(7),
            )
        )
        println(operator.toMatrix(operator.n))
    }

    @Test
    fun `svd reconstruction`() {
        hankelSVD(
            complexDoubleArrayOf(
                ComplexDouble(1),
                ComplexDouble(2),
                ComplexDouble(3),
                ComplexDouble(4),
                ComplexDouble(5),
                ComplexDouble(6),
                ComplexDouble(7),
            ),
            k = 1
        ).let { println(it) }
    }
}