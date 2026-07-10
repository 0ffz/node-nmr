package me.dvyy.nmr.ui.nodes

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.wavelet.WaveletHelpers

class WaveletTransformation() : SignalTransformation() {
    var threshold = mutableStateOf(0.0001)

    override val mutableStates: List<MutableState<*>> = listOf(threshold)

//    private val size by derivedStateOf { input?.fid?.size ?: 0 }
    private val inputFftRe by derivedStateOf { input?.fft?.real() }
    private val inputFftIm by derivedStateOf { input?.fft?.im() }

    override val output: State<Signal?> = derivedStateOf {
        val fftRe = inputFftRe ?: return@derivedStateOf null
        val fftIm = inputFftIm ?: return@derivedStateOf null
        val denoisedRe = WaveletHelpers.waveletDenoise(
            threshold.value,
            fftRe
        )
        val denoisedIm = WaveletHelpers.waveletDenoise(
            threshold.value,
            fftIm
        )

        Signal.Fft(ComplexDoubleArray.from(denoisedRe, denoisedIm))
    }
}
