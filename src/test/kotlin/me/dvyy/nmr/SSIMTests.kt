package me.dvyy.nmr

import io.kotest.matchers.shouldBe
import me.dvyy.nmr.evaluation.SSIM
import me.dvyy.nmr.evaluation.SSIM.of
import org.junit.jupiter.api.Test

class SSIMTests {
    @Test
    fun `bounded ssim of the same array should be 1`() {
        val signal = doubleArrayOf(1.1, 1.9, 3.2, 3.8, 5.1)
        val ssimValue = SSIM.bounded(signal, signal)
        ssimValue shouldBe 1.0
    }
}