package me.dvyy.nmr.ui.nodes.transformations

import androidx.compose.runtime.*
import imgui.ImGui
import imgui.flag.ImGuiTreeNodeFlags
import kotlinx.coroutines.Deferred
import me.dvyy.nmr.bindings.helpers.memScoped
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.bindings.propack.Propack
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.phasecorrect.findOptimalPhaseParameters
import me.dvyy.nmr.phasecorrect.phaseCorrect
import me.dvyy.nmr.signal.Signal
import me.dvyy.nmr.signal.SignalUiState
import me.dvyy.nmr.signal.expApodized
import me.dvyy.nmr.signal.gaussApodized
import me.dvyy.nmr.svd.HankelOperator
import me.dvyy.nmr.svd.MathHelpers
import me.dvyy.nmr.svd.reconstructDiagonals
import me.dvyy.nmr.synthetic.Resonance
import me.dvyy.nmr.synthetic.addGaussianNoise
import me.dvyy.nmr.synthetic.generateNmrSignal
import kotlin.random.Random

class ApodizationTransformation : SignalTransformation() {
    override val name: String = "Apodization"
    var lb by mutableStateOf(0.0001)
    var gauss by mutableStateOf(0.0)

    override fun ImGuiKt.drawParams() {
        dragDouble("lb", lb, onChange = { lb = it })
        dragDouble("gauss", gauss, onChange = { gauss = it })
    }

    private val size by derivedStateOf { input?.fid?.size ?: 0 }

    //    private val cache by derivedStateOf { ComplexDoubleArray(size) }
    private val inputFid by derivedStateOf { input?.fid?.data }

    override fun transform(): Deferred<Signal>? {
        if (size == 0) return null
        val input = inputFid
        val lb = lb
        val gauss = gauss
        return compute {
            val cache = ComplexDoubleArray(size)
            input?.copyInto(cache.data)
            cache.expApodized(lb).gaussApodized(gauss)
            Signal.Fid(cache)
        }
    }
}


class ZeroFillTransformation : SignalTransformation() {
    override val name: String = "Zero-fill"
    private val size by derivedStateOf { input?.fid?.size ?: 0 }

    override fun transform(): Deferred<Signal>? {
        if (size == 0) return null
        return compute {
            val target = MathHelpers.nextPowerOfTwo(size + 1000)
            val cache = ComplexDoubleArray(target)
            cache.data.fill(0.0)
            input?.fid?.data?.copyInto(cache.data)
            Signal.Fid(cache)
        }
    }
}

class PhaseCorrectTransformation : SignalTransformation() {
    override val name: String = "Phase"
    private val size by derivedStateOf { input?.fid?.size ?: 0 }
//    private val cache by derivedStateOf { ComplexDoubleArray(size) }
//    val p0 = mutableStateOf(0.0)
//    val p1 = mutableStateOf(0.0)
//
//    override val parameters: List<NodeAttribute> = listOf(
//        NodeAttribute("p0", p0),
//        NodeAttribute("p1", p1)
//    )

    override fun transform(): Deferred<Signal>? {
        if (size == 0) return null
//        this.p0.value = p0
//        this.p1.value = p1
        val fft = input?.fft?.data ?: return null
        return compute {
            val cache = ComplexDoubleArray(size)
            fft?.copyInto(cache.data)
            val (p0, p1) = cache.findOptimalPhaseParameters()
            Signal.Fft(cache.phaseCorrect(p0, p1))
        }
    }
}


class SVDTransformation : SignalTransformation() {
    override val name: String = "SVD"
    var numValues by mutableStateOf(10)

    override fun ImGuiKt.drawParams() {
        sliderInt("numValues", numValues, min = 1, max = 100, onChange = { numValues = it })
    }

    // TODO long-running background calculations
    override fun transform(): Deferred<Signal>? {
        val fid = input?.fid ?: return null
        val rows = fid.size / 2
        val cols = fid.size - rows + 1
        val numValues = numValues
        return compute {
            val denoised = memScoped {
//                val hankel = HankelOperatorBruteForce(fid.toMemorySegment())
                val hankel = HankelOperator(this, fid.toMemorySegment(), rows, cols)
                val result = Propack.partialComplexSVD(hankel, rows, cols, numWanted = numValues)
//            svdResults += result.singularValues
                result.reconstructDiagonals()
            }
            Signal.Fid(denoised)
        }
//        denoised[0] /= 2
    }
}

class SyntheticDataset : SignalProviding {
    override val name: String = "Synthetic"
    var dwellTime by mutableStateOf(0.001)
    var peaks = mutableStateListOf(
        Resonance(amplitude = 5.0, frequencyHz = -50.0, phaseRadians = 0.0, t2StarSeconds = 0.1)
    )
    var noise by mutableStateOf(0.0)
    var seed by mutableStateOf(Random.nextLong())
    val fid by derivedStateOf<ComplexDoubleArray> {
        generateNmrSignal(
            16384,
            dwellTime,
            peaks,
        ).addGaussianNoise(noise, seed)
    }

    fun ImGuiKt.resonance(resonance: Resonance, onChange: (Resonance) -> Unit) {
        dragDouble("amplitude", resonance.amplitude, onChange = { onChange(resonance.copy(amplitude = it)) })
        dragDouble("freq", resonance.frequencyHz, scaleNearZero = false, onChange = { onChange(resonance.copy(frequencyHz = it)) })
        dragDouble("phase", resonance.phaseRadians, onChange = { onChange(resonance.copy(phaseRadians = it)) })
        dragDouble("t2*", resonance.t2StarSeconds, scaleNearZero = false, onChange = { onChange(resonance.copy(t2StarSeconds = it)) })
    }
    override fun ImGuiKt.drawParams() {
        peaks.forEachIndexed { index, resonance ->
            treeNode("Peak $index", flags = ImGuiTreeNodeFlags.SpanTextWidth, header = {
                ImGui.sameLine()
                button(" - ##$index") { peaks.removeAt(index) }
            }) {
                resonance(resonance) {
                    peaks[index] = it
                }
            }
        }
        button("+", onClick = { peaks.add(Resonance(amplitude = 5.0, frequencyHz = -50.0, phaseRadians = 0.0, t2StarSeconds = 0.1)) })
        dragDouble("dwellTime", dwellTime, onChange = { dwellTime = it })
        dragDouble("noise", noise, onChange = { noise = it })
    }

    override val output: State<SignalUiState?> = derivedStateOf {
        SignalUiState(Signal.Fid(fid))
    }
}
