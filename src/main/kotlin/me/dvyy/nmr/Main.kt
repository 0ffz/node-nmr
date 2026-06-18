package me.dvyy.nmr

import imgui.ImFontConfig
import imgui.ImGui
import imgui.app.Application
import imgui.app.Configuration
import imgui.extension.implot.ImPlot
import imgui.flag.ImGuiConfigFlags
import imgui.flag.ImGuiWindowFlags
import me.dvyy.nmr.complex.ComplexDouble
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.complex.toComplexArray
import me.dvyy.nmr.parsing.BrukerDataset
import me.dvyy.nmr.parsing.removeDigitalFilter
import me.dvyy.nmr.propack.HankelOperator
import me.dvyy.nmr.propack.propack
import me.dvyy.nmr.svd.reconstructDiagonals
import org.apache.commons.math3.complex.Complex
import org.apache.commons.math3.transform.DftNormalization
import org.apache.commons.math3.transform.FastFourierTransformer
import org.apache.commons.math3.transform.TransformType.FORWARD
import org.jetbrains.bio.viktor.asF64Array
import java.io.IOException
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.math.exp

class Main : Application() {
    override fun initImGui(config: Configuration?) {
        super.initImGui(config)

        val io = ImGui.getIO();

//        io.fonts.setFreeTypeRenderer(true);
        io.iniFilename = null;                                // We don't want to save .ini file
        io.fonts.setFreeTypeRenderer(true)
        io.fonts.addFontFromMemoryTTF(loadFromResources("/NotoSans.ttf"), 24f, ImFontConfig())
        io.fonts.build()
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);  // Enable Keyboard Controls
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);      // Enable Docking
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);
//        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard);  // Enable Keyboard Controls
//        io.addConfigFlags(ImGuiConfigFlags.DockingEnable);      // Enable Docking
//        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable);    // Enable Multi-Viewport / Platform Windows
//        io.configViewportsNoTaskBarIcon = true;
        init()

    }

    private var fid = doubleArrayOf()
    private var fidDenoised = doubleArrayOf()
    private var fidIm = doubleArrayOf()
    private var fft = doubleArrayOf()
    private var fftDenoised = doubleArrayOf()

    fun init() {
        val brukerData = BrukerDataset("/var/home/offz/projects/nmr-kotlin/data/1d_carbon_ML/10")
        val cleanData = BrukerDataset("/var/home/offz/projects/nmr-kotlin/data/1d_carbon_ML/5")
        val cleanFid = cleanData.readFid().removeDigitalFilter(brukerData.acqus)
            .takeComplex(4096)

//            .expApodization(0.0005)
        // 1. You can freely inspect text parameters
        println("Pulse Program: ${brukerData.acqus["PULPROG"]}")

        println("Spectrometer Frequency: ${brukerData.acqus["SFO1"]} MHz")
        // 2. Load the 1D FID
        val fid = brukerData.readFid()
            .removeDigitalFilter(brukerData.acqus)
            .takeComplex(4096)
            .expApodization(0.00008)
        val hankel = HankelOperator(fid.toMemorySegment())
        val rows = fid.size / 2
        val cols = fid.size - rows + 1
        val result = propack(hankel, rows, cols, numWanted = 13)
        println(result.singularValues.toList())
        val denoised = result.reconstructDiagonals()
//        return
//            fft = fid.map { it.re }.toDoubleArray()
////            fidIm = fid.map { it.im }.toDoubleArray()
//        val denoised = hankelSVD(fid.takeComplex(2048), k = 12)
////        val denoised = fid
//        denoised[0] = denoised[0] / 2
//            fft = denoised.map { it.re }.toDoubleArray()
        val fourier = FastFourierTransformer(DftNormalization.UNITARY)
//        this@Main.fid = fid.real()
        fft = fourier.transform(cleanFid.toApache(), FORWARD).toKotlin().fftShift().abs().reversedArray()
        fft.asF64Array().let {  it /= it.max() }
        fidDenoised = denoised.abs()
        fftDenoised = fourier.transform(denoised.toApache(), FORWARD).toKotlin().fftShift().abs().reversedArray()
        fftDenoised.asF64Array().let {  it /= it.max() }
    }

    override fun process() {
        val viewport = ImGui.getMainViewport()
        ImGui.setNextWindowPos(viewport.posX, viewport.posY)
        ImGui.setNextWindowSize(viewport.sizeX, viewport.sizeY)
        if (ImGui.begin(
                "Demo",
                ImGuiWindowFlags.NoDecoration or ImGuiWindowFlags.NoMove or ImGuiWindowFlags.NoResize
            )
        ) {
            ImPlot.beginPlot("Hello world", ImGui.getContentRegionAvailX(), ImGui.getContentRegionAvailY())
            ImPlot.plotLine("Original", fft)
            ImPlot.plotLine("Denoised", fftDenoised)
            ImPlot.endPlot()
        }
        ImGui.end()
    }

    private fun loadFromResources(name: String): ByteArray {
        try {
            val resource = Main::class.java.getResource(name) ?: throw IOException("Resource not found: $name")
            return Files.readAllBytes(Paths.get(resource.toURI()))
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }
}
//
//fun ComplexDoubleArray.toOjalgo(): Array1D<ComplexNumber> = Array1D.C128.make(size).also { arr ->
//    forEachIndexed { index, complex -> arr[index] = ComplexNumber.of(complex.re, complex.im) }
//}

fun ComplexDoubleArray.fftShift(): ComplexDoubleArray {
    val n = this.size
    val half = n / 2
    val shifted = ComplexDoubleArray(n)

    var index = 0
    // Move the second half to the front, and the first half to the back
    for (i in half until n) shifted[index++] = this[i]
    for (i in 0 until half) shifted[index++] = this[i]

    return shifted
}

fun ComplexDoubleArray.toApache() = Array(size) { Complex(this[it].re, this[it].im) }
fun Array<Complex>.toKotlin() = ComplexDoubleArray(size) { ComplexDouble(this[it].real, this[it].imaginary) }
fun ComplexDoubleArray.expApodization(lb: Double): ComplexDoubleArray {
    return this.mapIndexed { index, complex ->
        val decay = exp(-Math.PI * lb * index)
        ComplexDouble(complex.re * decay, complex.im * decay)
    }.toComplexArray()
}

fun ComplexDoubleArray.takeComplex(n: Int): ComplexDoubleArray {
    return ComplexDoubleArray(n) { this[it] }
}

fun main() {
    ImPlot.createContext()
    Application.launch(Main())
}