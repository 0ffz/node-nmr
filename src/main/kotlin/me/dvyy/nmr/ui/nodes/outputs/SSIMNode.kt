package me.dvyy.nmr.ui.nodes.outputs

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.bindings.imgui.ImNodeContext
import me.dvyy.nmr.evaluation.SSIM
import me.dvyy.nmr.signal.SignalUiState
import me.dvyy.nmr.ui.nodes.Node

class SSIMNode: Node() {
    override val name: String = "SSIM"
    val input = inputAttribute<SignalUiState?>()
    val reference = inputAttribute<SignalUiState?>()
    val ssim by derivedStateOf {
        val inp = input.value?.graphFft ?: return@derivedStateOf null
        val ref = reference.value?.graphFft ?: return@derivedStateOf null
        SSIM.windowed(inp, ref)
    }
    override fun ImGuiKt.draw() {
        with(ImNodeContext) {
            inputAttribute(input.id) { text("Input") }
            inputAttribute(reference.id) { text("Reference") }
        }

        text("SSIM: $ssim")
    }
}