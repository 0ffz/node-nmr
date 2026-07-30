package me.dvyy.nmr.bindings.propack

import me.dvyy.nmr.common.math.ComplexDoubleMatrix

class SVDResult(
    val rows: Int,
    val cols: Int,
    val u: ComplexDoubleMatrix,
    val singularValues: DoubleArray,
    val v: ComplexDoubleMatrix,
)