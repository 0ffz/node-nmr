package me.dvyy.nmr.app.nodes.data.transformations

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import kotlinx.coroutines.Deferred
import me.dvyy.nmr.app.nodes.data.NodeInfo
import me.dvyy.nmr.app.nodes.data.SignalTransformationNode
import me.dvyy.nmr.common.math.ComplexDoubleArray
import me.dvyy.nmr.processing.denoise.cadzow.MathHelpers
import me.dvyy.nmr.processing.model.Signal

class ZeroFillTransformation : SignalTransformationNode() {
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

    companion object : NodeInfo<ZeroFillTransformation> {
        override val name = "Zero-fill"
        override val category = "1D"
        override val subcategory = "Transformations"
        override val factory = ::ZeroFillTransformation
    }
}

fun ComplexDoubleArray.zeroFill(): ComplexDoubleArray {
    val target = MathHelpers.nextPowerOfTwo(size + 1000)
    val cache = ComplexDoubleArray(target)
    data.copyInto(cache.data)
    return cache
}