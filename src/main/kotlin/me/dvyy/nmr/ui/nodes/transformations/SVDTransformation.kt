package me.dvyy.nmr.ui.nodes.transformations

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Deferred
import me.dvyy.nmr.bindings.helpers.memScoped
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.bindings.propack.Propack
import me.dvyy.nmr.signal.Signal
import me.dvyy.nmr.svd.HankelOperator
import me.dvyy.nmr.svd.reconstructDiagonals

class SVDTransformation : SignalTransformationNode() {
    override val name: String = "SVD"
    var numValues by mutableStateOf(10)

    override fun ImGuiKt.draw() {
        drawInput()
        sliderInt("numValues", numValues, min = 1, max = 100, onChange = { numValues = it })
    }

    // TODO long-running background calculations
    override fun transform(): Deferred<Signal>? {
        val fid = input?.fid ?: return null
        val rows = fid.size / 2
        val cols = fid.size - rows + 1
        val numValues = numValues
        return compute {
            val denoised = memScoped {
//                val hankel = HankelOperatorBruteForce(fid.toMemorySegment())
                val hankel = HankelOperator(this, fid.toMemorySegment(), rows, cols)
                val result = Propack.partialComplexSVD(hankel, rows, cols, numWanted = numValues)
//            svdResults += result.singularValues
                result.reconstructDiagonals()
            }
            Signal.Fid(denoised)
        }
//        denoised[0] /= 2
    }
}