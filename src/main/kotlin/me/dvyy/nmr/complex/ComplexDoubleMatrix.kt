package me.dvyy.nmr.complex

import org.jetbrains.bio.viktor.F64Array

class ComplexDoubleMatrix(
    val columns: Array<ComplexDoubleArray>
) {
    init {
        if (columns.isNotEmpty()) {
            val firstSize = columns[0].size
            require(columns.all { it.size == firstSize }) { "All columns must have the same size" }
        }
    }

    val width = columns.size
    val height = columns[0].size

    operator fun get(column: Int): ComplexDoubleArray = columns[column]

    override fun toString(): String {
        if (columns.isEmpty()) return "[]"
        val rowCount = columns[0].size
        val sb = StringBuilder()
        for (i in 0 until rowCount) {
            val row = columns.map { it[i] }
            sb.append(row.joinToString(separator = "\t", prefix = "[", postfix = "]"))
            if (i < rowCount - 1) sb.append("\n")
        }
        return sb.toString()
    }
}