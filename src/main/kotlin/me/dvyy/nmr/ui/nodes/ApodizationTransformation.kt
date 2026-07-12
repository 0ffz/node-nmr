package me.dvyy.nmr.ui.nodes

import androidx.compose.runtime.*
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.signal.expApodized

class ApodizationTransformation() : SignalTransformation() {
    var lb = mutableStateOf(0.0001)

    override val parameters: List<Parameter> = listOf(
        Parameter("lb", lb)
    )

    private val size by derivedStateOf { input?.fid?.size ?: 0 }
    private val cache by derivedStateOf { ComplexDoubleArray(size) }
    private val inputFid by derivedStateOf { input?.fid?.data }

    override val output: State<Signal?> = derivedStateOf {
        if(size == 0) return@derivedStateOf null
        inputFid?.copyInto(cache.data)
        cache.expApodized(lb.value)
        Signal.Fid(cache)
    }
}

