package me.dvyy.nmr.ui.nodes.multiscan

import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.common.math.ComplexDoubleArray
import me.dvyy.nmr.processing.denoise.wavelet.MultiScanWaveletDenoise
import me.dvyy.nmr.processing.model.Signal
import me.dvyy.nmr.processing.model.SignalSet
import me.dvyy.nmr.signal.SignalUiState
import me.dvyy.nmr.ui.nodes.Node
import me.dvyy.nmr.ui.nodes.NodeInfo

class MultiScanWaveletNode() : Node() {
    val input = inputAttribute<SignalSet?>()
    val output = outputAttribute<SignalUiState?> {
        val inputs = input.value?.signals ?: return@outputAttribute null
        val denoisedRe = MultiScanWaveletDenoise.denoise(inputs.map { it.fft.real() })
        val denoisedIm = MultiScanWaveletDenoise.denoise(inputs.map { it.fft.im() })
        val signal = Signal.Fft(ComplexDoubleArray.from(denoisedRe, denoisedIm))
        SignalUiState(signal)
    }

    override fun ImGuiKt.draw() {
        inputOutput(input, output)
    }

    companion object : NodeInfo<MultiScanWaveletNode> {
        override val name = "Multi scan denoise"
        override val factory = ::MultiScanWaveletNode
    }
}
