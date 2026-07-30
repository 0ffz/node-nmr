package me.dvyy.nmr

import io.kotest.matchers.shouldBe
import me.dvyy.nmr.io.bruker.BrukerDataset
import me.dvyy.nmr.processing.evaluation.SSIM
import me.dvyy.nmr.processing.model.Signal
import org.junit.jupiter.api.Test

class SSIMTests {
    @Test
    fun `ssim of the same array should be 1`() {
        val signal = doubleArrayOf(1.1, 1.9, 3.2, 3.8, 5.1)
        val ssimValue = SSIM.of(signal, signal, 1.0)
        ssimValue shouldBe 1.0
    }

    @Test
    fun `evaluate ssim`() {
        val reference = Signal.Fid(BrukerDataset("/var/home/offz/projects/nmr-kotlin/data/1d_carbon_ML/2").readFid())
        val noisy = Signal.Fid(BrukerDataset("/var/home/offz/projects/nmr-kotlin/data/1d_carbon_ML/8").readFid())
//val params = reference.fft.findOptimalPhaseParameters()
        println(SSIM.windowed(noisy.fft.real(), reference.fft.real(), 11))
    }
}