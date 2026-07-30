package me.dvyy.nmr.app.graphs.ui.screens

import imgui.extension.implot.ImPlot
import imgui.extension.implot.ImPlotPoint
import imgui.extension.implot.flag.ImPlotAxis
import imgui.extension.implot.flag.ImPlotAxisFlags
import me.dvyy.nmr.app.bindings.imgui.ImGuiKt
import me.dvyy.nmr.app.bindings.imgui.ImplotSubplotFlags
import me.dvyy.nmr.app.graphs.data.Graph2DEmitting
import me.dvyy.nmr.app.nodes.data.Node

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