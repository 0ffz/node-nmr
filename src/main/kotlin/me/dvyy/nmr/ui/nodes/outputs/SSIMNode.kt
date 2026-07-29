package me.dvyy.nmr.ui.nodes.outputs

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.bindings.imgui.ImNodeContext
import me.dvyy.nmr.evaluation.SSIM
import me.dvyy.nmr.signal.SignalUiState
import me.dvyy.nmr.ui.nodes.Node

import me.dvyy.nmr.ui.nodes.NodeInfo

class SSIMNode: Node() {
    val input = inputAttribute<SignalUiState?>()
    val reference = inputAttribute<SignalUiState?>()
    val ssim by derivedStateOf {
        val inp = input.value?.graphFft ?: return@derivedStateOf null
        val ref = reference.value?.graphFft ?: return@derivedStateOf null
        SSIM.windowed(inp, ref)
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
        override val factory = ::SSIMNode
    }
}