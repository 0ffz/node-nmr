package me.dvyy.nmr.ui.nodes.transformations

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.Deferred
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.signal.Signal
import me.dvyy.nmr.wavelet.WaveletHelpers

class WaveletTransformation : SignalTransformation() {
    override val name: String = "Wavelet denoise"
    var threshold by mutableStateOf(0.0001)
    var level by mutableStateOf(4)

    override fun ImGuiKt.drawParams() {
        sliderInt("level", level, min = 1, max = 11, onChange = { level = it })
        dragDouble("threshold", threshold, onChange = {threshold = it})
    }

    private val inputFftRe by derivedStateOf { input?.fft?.real() }
    private val inputFftIm by derivedStateOf { input?.fft?.im() }

    override fun transform(): Deferred<Signal>? {
        val fftRe = inputFftRe ?: return null
        val fftIm = inputFftIm ?: return null
        if (fftRe.isEmpty()) return null
        threshold
        level

        return compute {
            val denoisedRe = WaveletHelpers.waveletDenoise(
                fftRe,
                threshold,
                level
            )
            val denoisedIm = WaveletHelpers.waveletDenoise(
                fftIm,
                threshold,
                level
            )
            Signal.Fft(ComplexDoubleArray.from(denoisedRe, denoisedIm))
        }
    }
}
