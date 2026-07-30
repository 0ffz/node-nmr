package me.dvyy.nmr.ui.nodes.transformations

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import kotlinx.coroutines.Deferred
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.common.math.ComplexDoubleArray
import me.dvyy.nmr.processing.denoise.wavelet.WaveletHelpers
import me.dvyy.nmr.processing.model.Signal
import me.dvyy.nmr.ui.nodes.NodeInfo
import me.dvyy.nmr.ui.nodes.nodeState

class WaveletTransformation : SignalTransformationNode() {
    var threshold by nodeState(0.0001)
    var level by nodeState(4)
    var wavelet by nodeState("bior2.2")

    override fun ImGuiKt.draw() {
        drawInput()
        sliderInt("level", level, min = 1, max = 11, onChange = { level = it })
        dragDouble("threshold", threshold, onChange = { threshold = it })
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
                level,
                wavelet
            )
            val denoisedIm = WaveletHelpers.waveletDenoise(
                fftIm,
                threshold,
                level,
                wavelet
            )
            Signal.Fft(ComplexDoubleArray.from(denoisedRe, denoisedIm))
        }
    }

    companion object : NodeInfo<WaveletTransformation> {
        override val name = "Wavelet denoise"
        override val factory = ::WaveletTransformation
    }
}
