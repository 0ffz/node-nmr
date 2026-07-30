package me.dvyy.nmr.ui.nodes.outputs

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.bindings.imgui.ImNodeContext
import me.dvyy.nmr.common.math.ComplexDoubleArray
import me.dvyy.nmr.processing.denoise.cadzow.MathHelpers
import me.dvyy.nmr.processing.transform.apodization.expApodized
import me.dvyy.nmr.processing.transform.apodization.sineBellApodized
import me.dvyy.nmr.ui.graphs.Texture
import me.dvyy.nmr.ui.graphs.fftEachRow
import me.dvyy.nmr.ui.graphs.transpose
import me.dvyy.nmr.ui.nodes.Graph2DEmitting
import me.dvyy.nmr.ui.nodes.Graph2DUiState
import me.dvyy.nmr.ui.nodes.Node
import me.dvyy.nmr.ui.nodes.nodeState
import me.dvyy.nmr.ui.nodes.transformations.zeroFill

class Graph2DNode : Node(), Graph2DEmitting {
    val input = inputAttribute<List<ComplexDoubleArray>>()
    val height by derivedStateOf { input.value?.size ?: 0 }
    val width by derivedStateOf { input.value?.first()?.size ?: 0 }
    val textureFid by derivedStateOf {
        Texture.newTexture(width, height)
    }
    val textureFft by derivedStateOf {
        Texture.newTexture(MathHelpers.nextPowerOfTwo(width + 1000), MathHelpers.nextPowerOfTwo(height + 1000))
    }
    var lb by nodeState(0.0)
    override val texture: Graph2DUiState? by derivedStateOf {
        val data =
            input.value?.map { ComplexDoubleArray(it.data.copyOf()).expApodized(lb) } ?: return@derivedStateOf null
        textureFid.uploadHeatmap(data)

        val fft: List<ComplexDoubleArray> = data.let { data ->
//    val params = data.first().expApodized(0.001).findOptimalPhaseParameters()
            data.map { it.zeroFill() }.fftEachRow()
                .transpose()
                .map { it.sineBellApodized().zeroFill() }.fftEachRow()
                .transpose()
        }
        textureFft.uploadHeatmap(fft)

        Graph2DUiState(fidTexture = textureFid, fftTexture = textureFft)
    }

    override fun ImGuiKt.draw() {
        with(ImNodeContext) {
            inputAttribute(input.id) { text("Input") }
        }
        dragDouble("lb", lb, onChange = { lb = it })
    }
}