package me.dvyy.nmr.common.math

fun List<ComplexDoubleArray>.transpose(): List<ComplexDoubleArray> {
    if (isEmpty()) return emptyList()
    val rows = size
    val cols = this[0].size
    return List(cols) { col ->
        val newArray = ComplexDoubleArray(rows)
        for (row in 0 until rows) {
            newArray[row] = this[row][col]
        }
        newArray
    }
}