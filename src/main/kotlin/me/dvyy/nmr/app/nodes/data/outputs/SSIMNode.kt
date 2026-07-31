package me.dvyy.nmr.app.nodes.data.outputs

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import me.dvyy.nmr.app.bindings.imgui.ImGuiKt
import me.dvyy.nmr.app.bindings.imgui.ImNodeContext
import me.dvyy.nmr.app.nodes.data.Node
import me.dvyy.nmr.app.nodes.data.NodeInfo
import me.dvyy.nmr.app.nodes.ui.state.SignalUiState
import me.dvyy.nmr.processing.evaluation.SSIM

class SSIMNode : Node() {
    val input = inputAttribute<SignalUiState?>()
    val reference = inputAttribute<SignalUiState?>()
    val ssim by derivedStateOf {
        val inp = input.value?.graphFft ?: return@derivedStateOf null
        val ref = reference.value?.graphFft ?: return@derivedStateOf null
        runCatching { SSIM.windowed(inp, ref) }.getOrNull()
    }

    override fun ImGuiKt.draw() {
        with(ImNodeContext) {
            inputAttribute(reference.id) { text("Reference") }
            inputAttribute(input.id) { text("Input") }
        }

        text("SSIM: $ssim")
    }

    companion object : NodeInfo<SSIMNode> {
        override val name = "SSIM"
        override val category = "1D"
        override val subcategory = "Outputs"
        override val factory = ::SSIMNode
    }
}