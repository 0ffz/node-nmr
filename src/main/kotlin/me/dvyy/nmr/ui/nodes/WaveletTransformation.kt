package me.dvyy.nmr.ui.nodes

import androidx.compose.runtime.*
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.wavelet.WaveletHelpers

class WaveletTransformation() : SignalTransformation() {
    var threshold = mutableStateOf(0.0001)
    var level = mutableStateOf(4)

    override val parameters: List<Parameter> = listOf(
        Parameter("threshold", threshold),
        Parameter("level", level)
    )

    private val inputFftRe by derivedStateOf { input?.fft?.real() }
    private val inputFftIm by derivedStateOf { input?.fft?.im() }

    override val output: State<Signal?> = derivedStateOf {
        val fftRe = inputFftRe ?: return@derivedStateOf null
        val fftIm = inputFftIm ?: return@derivedStateOf null
        if (fftRe.isEmpty()) return@derivedStateOf null
        val denoisedRe = WaveletHelpers.waveletDenoise(
            fftRe,
            threshold.value,
            level.value
        )
        val denoisedIm = WaveletHelpers.waveletDenoise(
            fftIm,
            threshold.value,
            level.value
        )

        Signal.Fft(ComplexDoubleArray.from(denoisedRe, denoisedIm))
    }
}
