package me.dvyy.nmr

import androidx.compose.runtime.mutableStateOf
import me.dvyy.nmr.complex.complexDoubleArrayOf
import me.dvyy.nmr.complex.j
import me.dvyy.nmr.ui.nodes.ApodizationTransformation
import me.dvyy.nmr.ui.nodes.Signal
import org.junit.jupiter.api.Test

class TransformationLinkingTests {
    @Test
    fun `should be able to link output of one transform into another`() {
        val input = mutableStateOf(Signal.Fid(complexDoubleArrayOf(1.j, 1.j, 1.j)))
        val apod = ApodizationTransformation()
        val apod2 = ApodizationTransformation()
        apod.inputRef = input
        apod2.inputRef = apod.output
        println(apod.output.value)
        println(apod2.output.value)
        input.value = Signal.Fid(complexDoubleArrayOf(1.j, 1.j))
        println(apod2.output.value)
    }
}
