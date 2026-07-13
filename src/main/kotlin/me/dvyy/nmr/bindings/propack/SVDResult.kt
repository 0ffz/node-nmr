package me.dvyy.nmr.bindings.propack

import me.dvyy.nmr.complex.ComplexDoubleMatrix

class SVDResult(
    val rows: Int,
    val cols: Int,
    val u: ComplexDoubleMatrix,
    val singularValues: DoubleArray,
    val v: ComplexDoubleMatrix,
)