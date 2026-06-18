package me.dvyy.nmr.svd

import me.dvyy.nmr.complex.ComplexDouble
import me.dvyy.nmr.complex.ComplexDoubleArray

fun hankelSVD(originalData: ComplexDoubleArray, k: Int): ComplexDoubleArray {
//    val hankel = HankelOperator(originalData)
//    val size = hankel.n
//    val solver = MatrixDecomposition.eigshComplex(hankel, size, k, "LM", null, 100, 1e-5)
//    solver.solve()
//    val eigenvectors = solver.eigenvectors
//    val eigenvalues = solver.eigenvalues
//    val decomp = ComplexEigenDecomposition.of(eigenvalues, eigenvectors)
//    val reconstructed = decomp.reconstructDiagonals()
TODO()
//    if (originalData.size % 2 == 0) {
//        return reconstructed.plus(ComplexDouble.zero)
//    } else return reconstructed
}