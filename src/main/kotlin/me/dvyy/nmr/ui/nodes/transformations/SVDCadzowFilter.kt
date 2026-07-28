package me.dvyy.nmr.ui.nodes.transformations

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import imgui.ImVec2
import kotlinx.coroutines.Deferred
import me.dvyy.nmr.bindings.helpers.memScoped
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.bindings.propack.Propack
import me.dvyy.nmr.signal.Signal
import me.dvyy.nmr.svd.HankelOperator
import me.dvyy.nmr.svd.HankelOperatorBruteForce
import me.dvyy.nmr.svd.reconstructDiagonals
import java.lang.foreign.MemorySegment

import me.dvyy.nmr.ui.nodes.NodeInfo
import me.dvyy.nmr.ui.nodes.nodeState

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
