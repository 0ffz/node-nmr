package me.dvyy.nmr.ui.nodes.outputs

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import imgui.ImGui
import imgui.flag.ImGuiColorEditFlags
import imgui.type.ImBoolean
import imgui.type.ImString
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.bindings.imgui.ImNodeContext
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.phasecorrect.findOptimalPhaseParameters
import me.dvyy.nmr.signal.SignalUiState
import me.dvyy.nmr.signal.expApodized
import me.dvyy.nmr.signal.sineBellApodized
import me.dvyy.nmr.svd.MathHelpers
import me.dvyy.nmr.ui.graphs.Texture
import me.dvyy.nmr.ui.graphs.fftEachRow
import me.dvyy.nmr.ui.graphs.transpose
import me.dvyy.nmr.ui.nodes.*
import me.dvyy.nmr.ui.nodes.transformations.zeroFill
import java.awt.Color

class GraphNode : Node(), GraphEmitting {
    override val name: String = "Graph"
    var title by mutableStateOf("Untitled")
    var color by mutableStateOf<Color?>(null)
    val string = ImString(title, 42)
    var autoPhase by mutableStateOf(false)
    var yOffset by mutableStateOf(0.0)
    val input = inputAttribute<SignalUiState?>()

    override fun ImGuiKt.draw() {
        if (ImGui.inputText("Title", string)) {
            title = string.get()
        }
        with(ImNodeContext) {
            inputAttribute(input.id) { text("Input") }
        }
        colorEdit4("Color", color ?: Color.BLACK, onChange = { color = it }, flags = ImGuiColorEditFlags.NoInputs)
        val bool = ImBoolean(autoPhase)
        if (ImGui.checkbox("Auto Phase", bool)) {
            autoPhase = bool.get()
        }
        dragDouble("Y Offset", yOffset, scaleNearZero = false, onChange = { yOffset = it })
    }

    override val graph: GraphUiState? by derivedStateOf {
        val input = input.value ?: return@derivedStateOf null
        val phased = if (autoPhase) {
            input.signal.fft.findOptimalPhaseParameters()
        } else input.phaseParams
        GraphUiState(title, input.copy(phaseParams = phased, yOffset = yOffset), color)
    }
}

class Graph2DNode : Node(), Graph2DEmitting {
    override val name: String = "Graph 2D"

    val input = inputAttribute<List<ComplexDoubleArray>>()
    val height by derivedStateOf { input.value?.size ?: 0 }
    val width by derivedStateOf { input.value?.first()?.size ?: 0 }
    val textureFid by derivedStateOf {
        Texture.newTexture(width, height)
    }
    val textureFft by derivedStateOf {
        Texture.newTexture(MathHelpers.nextPowerOfTwo(width + 1000), MathHelpers.nextPowerOfTwo(height + 1000))
    }
    var lb by mutableStateOf(0.0)
    override val texture: Graph2DUiState? by derivedStateOf {
        val data = input.value?.map { ComplexDoubleArray(it.data.copyOf()).expApodized(lb) } ?: return@derivedStateOf null
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