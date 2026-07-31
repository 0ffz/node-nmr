package me.dvyy.nmr.app.nodes.data.outputs

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import me.dvyy.nmr.app.bindings.Texture
import me.dvyy.nmr.app.bindings.imgui.ImGuiKt
import me.dvyy.nmr.app.bindings.imgui.ImNodeContext
import me.dvyy.nmr.app.graphs.data.Graph2DEmitting
import me.dvyy.nmr.app.graphs.data.fftEachRow
import me.dvyy.nmr.app.graphs.ui.state.Graph2DUiState
import me.dvyy.nmr.app.nodes.data.Node
import me.dvyy.nmr.app.nodes.data.NodeInfo
import me.dvyy.nmr.app.nodes.data.parameters.nodeState
import me.dvyy.nmr.app.nodes.data.transformations.zeroFill
import me.dvyy.nmr.common.math.ComplexDoubleArray
import me.dvyy.nmr.common.math.transpose
import me.dvyy.nmr.processing.denoise.cadzow.MathHelpers
import me.dvyy.nmr.processing.transform.apodization.expApodized
import me.dvyy.nmr.processing.transform.apodization.sineBellApodized

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

    companion object : NodeInfo<Graph2DNode> {
        override val name = "Graph 2D"
        override val category = "2D"
        override val subcategory = "Outputs"
        override val factory = ::Graph2DNode
    }
}