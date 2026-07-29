package me.dvyy.nmr.ui.graphs

import imgui.extension.implot.ImPlot
import imgui.extension.implot.ImPlotPoint
import imgui.extension.implot.ImPlotSpec
import imgui.extension.implot.flag.ImPlotAxis
import imgui.extension.implot.flag.ImPlotAxisFlags
import imgui.extension.implot.flag.ImPlotItemFlags
import me.dvyy.nmr.bindings.fftw.FftwComplexArray
import me.dvyy.nmr.bindings.fftw.FftwDirection
import me.dvyy.nmr.bindings.fftw.FftwFlag
import me.dvyy.nmr.bindings.fftw.FftwPlan1D
import me.dvyy.nmr.bindings.helpers.memScoped
import me.dvyy.nmr.bindings.imgui.ImGuiKt
import imgui.ImGui
import me.dvyy.nmr.bindings.imgui.implotSpec
import me.dvyy.nmr.complex.ComplexDoubleArray
import me.dvyy.nmr.signal.Signal
import me.dvyy.nmr.signal.fftShift
import me.dvyy.nmr.ui.nodes.Graph2DEmitting
import me.dvyy.nmr.ui.nodes.GraphEmitting
import me.dvyy.nmr.ui.nodes.Node
import me.dvyy.nmr.ui.nodes.transformations.toImVec4
import org.lwjgl.BufferUtils
import org.lwjgl.opengl.GL11.*
import java.awt.Color
import java.nio.ByteBuffer
import java.util.*

fun List<ComplexDoubleArray>.fftEachRow(): List<ComplexDoubleArray> {
    val first = first()
    return memScoped {
        val size = first.size
        val input = FftwComplexArray(size)
        val output = FftwComplexArray(size)
        val plan = FftwPlan1D(size, input, output, FftwDirection.FORWARD, FftwFlag.ESTIMATE)
        map { fid ->
            val data = fid.data
            input.loadInterleaved(data)
            plan.execute()
            ComplexDoubleArray(output.toInterleavedArray()).fftShift()
        }
    }
}

fun List<ComplexDoubleArray>.transpose(): List<ComplexDoubleArray> {
    if (isEmpty()) return emptyList()
    val rows = size
    val cols = this[0].size
    return List(cols) { col ->
        val newArray = ComplexDoubleArray(rows)
        for (row in 0 until rows) {
            newArray[row] = this[row][col]
        }
        newArray
    }
}

class Texture(
    val id: Int,
    val width: Int,
    val height: Int,
) {
    val pixelBuffer = BufferUtils.createByteBuffer(width * height * 4)
    fun upload()/* = withContext(AppDispatchers.Frontend)*/ {
        glBindTexture(GL_TEXTURE_2D, id)
//        glTexSubImage2D(GL_TEXTURE_2D, 0, 0, 0, width, height, GL_RGBA, GL_UNSIGNED_BYTE, pixelBuffer)
        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, pixelBuffer);
        val error = glGetError()
        if (error != GL_NO_ERROR) {
            println("OpenGL Error during texture upload: $error")
        }
    }

    fun updateBuffer(block: ByteBuffer.() -> Unit) {
        pixelBuffer.clear()
        block(pixelBuffer)
        pixelBuffer.flip()
        upload()
    }

    fun uploadHeatmap(data: List<ComplexDoubleArray>) = updateBuffer {
        val max = data.flatMap { it.abs().toList() }.max()
        for (y in 0 until height) {
            for (x in 0 until width) {
                val value = data[y][x].abs()

                val color = valueToColor(value, max)

                put(color.red.toByte())
                put(color.green.toByte())
                put(color.blue.toByte())
                put(255.toByte())
            }
        }
    }

//    data class Color(val r: Int, val g: Int, val b: Int)

    fun valueToColor(value: Double, max: Double): Color {
        val normalized = (value / max).coerceIn(0.0, 1.0)
        val r = (normalized * 255).toInt()
        val g = ((normalized * normalized) * 255).toInt()
        val b = (Math.sin(normalized * Math.PI) * 255).toInt()
        return Color(r, g, b)
    }

    companion object {
        fun newTexture(
            width: Int, height: Int,
        ): Texture {
            val textureId = glGenTextures()// Setup filtering (NEAREST keeps the sharp "pixelated" heatmap look, LINEAR blurs it)
            glBindTexture(GL_TEXTURE_2D, textureId)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST)
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST)
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA8, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, 0L)
            return Texture(textureId, width, height)
        }
    }
}

fun ImGuiKt.Graph2DScreen(
    nodes: List<Node>,
) {
    subplots("##plots", rows = 1, cols = 2, flags = ImplotSubplotFlags.ShareItems or ImplotSubplotFlags.NoTitle) {
        plot("fid") {
            nodes.forEach { node ->
                if (node !is Graph2DEmitting) return@forEach
                val texture = node.texture?.fidTexture ?: return@forEach
                val boundsMin = ImPlotPoint(0.0, 0.0)
                val boundsMax = ImPlotPoint(texture.width.toDouble(), texture.width.toDouble())
                ImPlot.plotImage("Heatmap", texture.id.toLong(), boundsMin, boundsMax)
            }
        }
        plot("FFT") {
            ImPlot.setupAxis(ImPlotAxis.X1, "t2", ImPlotAxisFlags.Invert)
            ImPlot.setupAxis(ImPlotAxis.Y1, "t1")
            nodes.forEach { node ->
                if (node !is Graph2DEmitting) return@forEach
                val texture = node.texture?.fftTexture ?: return@forEach
                val boundsMin = ImPlotPoint(0.0, 0.0)
                val boundsMax = ImPlotPoint(texture.width.toDouble(), texture.width.toDouble())
                ImPlot.plotImage("Heatmap", texture.id.toLong(), boundsMin, boundsMax)
            }
        }
    }
}


fun ImGuiKt.GraphScreen(
    nodes: List<Node>,
    selectedPlots: EnumSet<GraphType>,
    onPlotsChange: (EnumSet<GraphType>) -> Unit,
) {
    if (ImGui.beginCombo("Visible Plots", selectedPlots.joinToString(", ") { it.name })) {
        GraphType.entries.forEach { type ->
            val isSelected = selectedPlots.contains(type)
            if (ImGui.selectable(type.name, isSelected)) {
                val newSelection = EnumSet.copyOf(selectedPlots)
                if (isSelected) newSelection.remove(type) else newSelection.add(type)
                if (newSelection.isNotEmpty()) {
                    onPlotsChange(newSelection)
                }
            }
        }
        ImGui.endCombo()
    }

    val rows = selectedPlots.size

    subplots("##plots", rows = rows, cols = 1) {//, flags = ImplotSubplotFlags.ShareItems or ImplotSubplotFlags.NoTitle) {
        if (selectedPlots.contains(GraphType.FID)) plot("Spectra") {
            nodes.forEach { node ->
                if (node !is GraphEmitting) return@forEach
                val graph = node.graph
                val signal = graph?.signal ?: return@forEach
                if (signal != Signal.Empty) {
                    val spec = implotSpec {
                        if (graph.color != null) lineColor = graph.color.toImVec4()
                    }
                    line(graph.title, signal.graphFid, spec = spec)
                }
            }
        }
        if (selectedPlots.contains(GraphType.FFT)) plot("FFT") {
            ImPlot.setupAxis(ImPlotAxis.X1, "ppm", ImPlotAxisFlags.Invert)
            nodes.forEach { node ->
                if (node !is GraphEmitting) return@forEach
                val graph = node.graph
                val signal = graph?.signal ?: return@forEach
                if (signal != Signal.Empty) {
                    val spec = implotSpec {
                        if (graph.color != null) lineColor = graph.color.toImVec4()
                        lineWeight = 2f
                    }
                    line(
                        graph.title,
                        signal.graphFft,
                        xStart = signal.offset - signal.widthPPM,
                        xScale = signal.widthPPM / signal.graphFft.size,
                        spec = spec
                    )
                }
            }
        }
        if (selectedPlots.contains(GraphType.WAVELET)) plot("Wavelet") {
            nodes.forEach { node ->
                if (node !is GraphEmitting) return@forEach
                val graph = node.graph
                val signal = graph?.signal ?: return@forEach
                if (signal != Signal.Empty) {
                    val spec = implotSpec {
                        if (graph.color != null) lineColor = graph.color.toImVec4()
                    }
                    ImPlot.plotInfLines("Levels", signal.waveletLevels, ImPlotSpec().apply {
                        flags = ImPlotItemFlags.NoLegend
//                        lineColor = ImVec4(0.9f, 0.9f, 0.9f, 1f)
                    })
                    line(graph.title, signal.graphWavelet, spec = spec)
                }
            }
        }
    }
}
