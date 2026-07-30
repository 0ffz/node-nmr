package me.dvyy.nmr.ui.nodes.outputs

import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.writeString
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import me.dvyy.nmr.app.dispatchers.AppDispatchers
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.bindings.imgui.ImNodeContext
import me.dvyy.nmr.signal.SignalUiState
import me.dvyy.nmr.ui.nodes.Node
import me.dvyy.nmr.ui.nodes.NodeInfo

class ExportNode : Node() {
    val input = inputAttribute<SignalUiState?>()

    override fun ImGuiKt.draw() {
        with(ImNodeContext) {
            inputAttribute(input.id) { text("Input") }
        }

        button("Export as json") {
            val fft = input.value?.graphFft ?: return@button
            val targetSize = 1024
            val downsampled = List(targetSize) { i ->
                fft[(i * fft.size / targetSize).coerceAtMost(fft.size - 1)]
            }
            AppDispatchers.scope.launch {
                val file = FileKit.openFileSaver("data", defaultExtension = "json") ?: return@launch
                val json = Json.encodeToString(
                    GraphData(
                        freq = List(targetSize) { it.toDouble() },
                        magnitude = downsampled
                    )
                )
                file.writeString(json)
            }
        }
    }

    companion object : NodeInfo<ExportNode> {
        override val name = "Export"
        override val factory = ::ExportNode
    }
}

@Serializable
data class GraphData(
    val freq: List<Double>,
    val magnitude: List<Double>,
)