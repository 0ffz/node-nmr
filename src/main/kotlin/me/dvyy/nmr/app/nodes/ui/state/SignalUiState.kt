package me.dvyy.nmr.app.nodes.ui.state

import me.dvyy.nmr.processing.model.Signal
import me.dvyy.nmr.processing.transform.phase.PhaseParams
import me.dvyy.nmr.processing.transform.phase.phaseCorrect
import org.jetbrains.bio.viktor.asF64Array

data class SignalUiState(
    val signal: Signal,
    val offset: Double = 0.0,
    val widthPPM: Double = 1.0,
    val yOffset: Double = 0.0,
    val phaseParams: PhaseParams = PhaseParams(0.0, 0.0),
) {
    val graphFid: DoubleArray by lazy {
        if (signal.fid.size == 0) return@lazy doubleArrayOf()
        signal.fid.real().also { it.asF64Array().let { it /= it.max() } }
    }
    val graphFft: DoubleArray by lazy {
        if (signal.fft.size == 0) return@lazy doubleArrayOf()
        val (p0, p1) = phaseParams
        val fft = signal.fft.phaseCorrect(p0, p1).real()
        fft.asF64Array() += yOffset
        fft
    }
    val graphWavelet: DoubleArray by lazy { signal.wavelet.real() }
    val waveletLevels: IntArray by lazy {
        val sections = signal.waveletLevels + 1
        val levelLength = signal.wavelet.size / sections
        IntArray(sections - 1) {
            (it + 1) * levelLength
        }
    }
}