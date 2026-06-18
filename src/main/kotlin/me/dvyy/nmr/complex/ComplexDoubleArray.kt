package me.dvyy.nmr.complex

import java.lang.foreign.MemorySegment

@JvmInline
value class ComplexDoubleArray(
    @JvmField
    val data: DoubleArray,
) {
    constructor(size: Int) : this(DoubleArray(size * 2))

    constructor(size: Int, init: (Int) -> ComplexDouble) : this(DoubleArray(size * 2)) {
        for (i in 0 until size) {
            val value = init(i)
            set(i, value)
        }
    }

    init {
        require(data.size % 2 == 0) { "Data size must be divisible by 2" }
    }

    /**
     * Returns the complex element at the given [index].
     *
     * @throws IndexOutOfBoundsException if [index] is negative or >= [size].
     */
    public operator fun get(index: Int): ComplexDouble {
        checkElementIndex(index, size)
        val i = index shl 1
        return ComplexDouble(data[i], data[i + 1])
    }

    /**
     * Sets the complex element at the given [index] to [value].
     *
     * @throws IndexOutOfBoundsException if [index] is negative or >= [size].
     */
    public operator fun set(index: Int, value: ComplexDouble): Unit {
        checkElementIndex(index, size)
        val i = index shl 1
        data[i] = value.re
        data[i + 1] = value.im
    }

    val size get() = data.size / 2

    inline fun forEach(action: (ComplexDouble) -> Unit) {
        for (i in 0 until size) {
            action(get(i))
        }
    }

    inline fun forEachIndexed(action: (index: Int, ComplexDouble) -> Unit) {
        for (i in 0 until size) {
            action(i, get(i))
        }
    }

    inline fun <R> mapIndexed(map: (index: Int, ComplexDouble) -> R): List<R> {
        val list = ArrayList<R>(size)
        for (i in 0 until size) {
            list.add(map(i, get(i)))
        }
        return list
    }

    inline fun <R> map(map: (ComplexDouble) -> R): List<R> {
        val list = ArrayList<R>(size)
        forEach { list.add(map(it)) }
        return list
    }

    fun real() = map { it.re }.toDoubleArray()
    fun abs() = map { it.abs() }.toDoubleArray()
    fun im() = map { it.im }.toDoubleArray()

    fun toList() = map { it }

    inline fun getRe(index: Int) = data[index * 2]
    inline fun getIm(index: Int) = data[index * 2 + 1]

    inline fun setRe(index: Int, value: Double) {
        data[index * 2] = value
    }
    inline fun setIm(index: Int, value: Double) {
        data[index * 2 + 1] = value
    }
    /**
     * Converts to interleaved complex memory segment
     */
    fun toMemorySegment(): MemorySegment {
        return MemorySegment.ofArray(data)
    }
    @Suppress("DuplicatedCode")
    override fun toString(): String {
        val sb = StringBuilder(2 + data.size * 3)
        sb.append("[")
        var i = 0
        while (i < data.size) {
            if (i > 0) sb.append(", ")
            sb.append("${data[i]} + ${data[++i]}i")
            i++
        }
        sb.append("]")
        return sb.toString()
    }

    fun plus(vararg elements: ComplexDouble): ComplexDoubleArray {
        val newSize = size + elements.size
        val newData = DoubleArray(newSize * 2)
        System.arraycopy(data, 0, newData, 0, data.size)
        for (i in elements.indices) {
            newData[data.size + (i shl 1)] = elements[i].re
            newData[data.size + (i shl 1) + 1] = elements[i].im
        }
        return ComplexDoubleArray(newData)
    }
}

private fun checkElementIndex(index: Int, size: Int) {
    if (index < 0 || index >= size) throw IndexOutOfBoundsException("index: $index, size: $size")
}

fun DoubleArray.asComplexInterweaved(): ComplexDoubleArray = ComplexDoubleArray(this)

fun Collection<ComplexDouble>.toComplexArray() = ComplexDoubleArray(size).apply {
    for(i in indices) {
        this[i] = this@toComplexArray.elementAt(i)
    }
}

fun complexDoubleArrayOf(vararg values: ComplexDouble) = ComplexDoubleArray(values.size).apply {
    for(i in values.indices) {
        this[i] = values[i]
    }
}