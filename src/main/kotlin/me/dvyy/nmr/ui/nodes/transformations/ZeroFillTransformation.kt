package me.dvyy.nmr.ui.nodes.transformations

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import kotlinx.coroutines.Deferred
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.signal.Signal
import me.dvyy.nmr.svd.MathHelpers

class ZeroFillTransformation : SignalTransformationNode() {
    override val name: String = "Zero-fill"
    private val size by derivedStateOf { input?.fid?.size ?: 0 }

    override fun transform(): Deferred<Signal>? {
        if (size == 0) return null
        return compute {
            val target = MathHelpers.nextPowerOfTwo(size + 1000)
            val cache = ComplexDoubleArray(target)
            cache.data.fill(0.0)
            input?.fid?.data?.copyInto(cache.data)
            Signal.Fid(cache)
        }
    }
}

fun ComplexDoubleArray.zeroFill(): ComplexDoubleArray {
    val target = MathHelpers.nextPowerOfTwo(size + 1000)
    val cache = ComplexDoubleArray(target)
    data.copyInto(cache.data)
    return cache
}