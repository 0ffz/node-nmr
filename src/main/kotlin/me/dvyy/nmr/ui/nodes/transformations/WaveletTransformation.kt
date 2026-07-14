package me.dvyy.nmr.ui.nodes.transformations

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Deferred
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.signal.Signal
import me.dvyy.nmr.ui.nodes.NodeAttribute
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

    override fun transform(): Deferred<Signal>? {
        val fftRe = inputFftRe ?: return null
        val fftIm = inputFftIm ?: return null
        if (fftRe.isEmpty()) return null
        threshold.value
        level

        return compute {
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
}
