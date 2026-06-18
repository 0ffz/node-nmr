package me.dvyy.nmr.complex

class ComplexDoubleMatrix(
    val columns: Array<ComplexDoubleArray>
) {
    init {
        if (columns.isNotEmpty()) {
            val firstSize = columns[0].size
            require(columns.all { it.size == firstSize }) { "All columns must have the same size" }
        }
    }

    val rows = columns[0].size

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