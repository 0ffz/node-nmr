package me.dvyy.nmr.app.nodes.data.transformations

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import imgui.ImVec2
import kotlinx.coroutines.Deferred
import me.dvyy.nmr.app.bindings.imgui.ImGuiKt
import me.dvyy.nmr.app.nodes.data.NodeInfo
import me.dvyy.nmr.app.nodes.data.SignalTransformationNode
import me.dvyy.nmr.app.nodes.data.parameters.nodeState
import me.dvyy.nmr.bindings.common.memScoped
import me.dvyy.nmr.bindings.propack.Propack
import me.dvyy.nmr.processing.denoise.cadzow.HankelOperator
import me.dvyy.nmr.processing.denoise.cadzow.reconstructDiagonals
import me.dvyy.nmr.processing.model.Signal

class SVDCadzowFilter : SignalTransformationNode() {
    var numValues by nodeState(10)
    var singularValues by mutableStateOf(doubleArrayOf())

    override fun ImGuiKt.draw() {
        drawInput()
        sliderInt("numValues", numValues, min = 1, max = 100, onChange = { numValues = it })

        plot("Singular values", ImVec2(200f, 200f)) {
            line("values", singularValues)
        }
    }

    // TODO long-running background calculations
    override fun transform(): Deferred<Signal>? {
        val fid = input?.fid ?: return null
        val rows = fid.size / 2
        val cols = fid.size - rows + 1
        val numValues = numValues
        return compute {
            val denoised = memScoped {
                val hankel = HankelOperator(this, fid.asMemorySegmentCopy(), rows, cols)
                val result = Propack.partialComplexSVD(hankel, rows, cols, numWanted = numValues)
                singularValues = result.singularValues
                result.reconstructDiagonals()
            }
            Signal.Fid(denoised)
        }
//        denoised[0] /= 2
    }


    companion object : NodeInfo<SVDCadzowFilter> {
        override val name = "SVD"
        override val factory = ::SVDCadzowFilter
    }
}
