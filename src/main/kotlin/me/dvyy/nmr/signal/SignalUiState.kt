package me.dvyy.nmr.signal

import me.dvyy.nmr.phasecorrect.PhaseParams
import me.dvyy.nmr.phasecorrect.phaseCorrect
import org.jetbrains.bio.viktor.asF64Array

data class SignalUiState(
    val signal: Signal,
    val offset: Double = 0.0,
    val phaseParams: PhaseParams = PhaseParams(0.0, 0.0),
) {
    val graphFid: DoubleArray by lazy {
        if (signal.fid.size == 0) return@lazy doubleArrayOf()
        signal.fid.real().also { it.asF64Array().let { it /= it.max() } }
    }
    val graphFft: DoubleArray by lazy {
        if (signal.fft.size == 0) return@lazy doubleArrayOf()
        val (p0, p1) = phaseParams
        signal.fft.phaseCorrect(p0, p1).real()
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