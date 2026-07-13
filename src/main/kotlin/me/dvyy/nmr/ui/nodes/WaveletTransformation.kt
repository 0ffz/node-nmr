package me.dvyy.nmr.ui.nodes

import androidx.compose.runtime.*
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.wavelet.WaveletHelpers

class WaveletTransformation() : SignalTransformation() {
    override val name: String = "Wavelet denoise"
    var threshold = mutableStateOf(0.0001)
    var level by mutableStateOf(4)

    override val parameters: List<NodeAttribute> = listOf(
        NodeAttribute("threshold", threshold),
    )

    override fun ImGuiKt.drawParams() {
        sliderInt("level", level, min = 1, max = 11, onChange = { level = it })
    }

    private val inputFftRe by derivedStateOf { input?.fft?.real() }
    private val inputFftIm by derivedStateOf { input?.fft?.im() }

    override val output: State<Signal?> = derivedStateOf {
        val fftRe = inputFftRe ?: return@derivedStateOf null
        val fftIm = inputFftIm ?: return@derivedStateOf null
        if (fftRe.isEmpty()) return@derivedStateOf null
        val denoisedRe = WaveletHelpers.waveletDenoise(
            fftRe,
            threshold.value,
            level
        )
        val denoisedIm = WaveletHelpers.waveletDenoise(
            fftIm,
            threshold.value,
            level
        )

        Signal.Fft(ComplexDoubleArray.from(denoisedRe, denoisedIm))
    }
}
