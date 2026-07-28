package me.dvyy.nmr.ui.nodes

import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.reflect.KType
import kotlin.reflect.full.isSubtypeOf

interface Attribute<T> {
    val id: Int
}

class InputAttribute<T>(
    override val id: Int,
    val localId: Int,
    val type: KType
): Attribute<T> {
    private val emptyState = mutableStateOf(null)
    internal var inputRef by mutableStateOf<State<T?>>(emptyState)
    val value: T? by derivedStateOf { inputRef.value }

    fun removePipe() {
        inputRef = emptyState
    }
}

class OutputAttribute<T>(
    override val id: Int,
    val localId: Int,
    val type: KType,
    val calculation: ()-> T,
): Attribute<T> {
    val output: State<T?> = derivedStateOf { calculation() }

    fun pipeInto(input: InputAttribute<*>): Boolean {
        if (!type.isSubtypeOf(input.type)) {
            println("Type mismatch: $type, ${input.type}")
            return false
        }
        val input = input as InputAttribute<T>
        input.inputRef = output
        return true
    }
}