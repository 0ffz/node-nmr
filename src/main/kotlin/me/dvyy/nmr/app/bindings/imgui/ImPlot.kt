package me.dvyy.nmr.app.bindings.imgui

import imgui.extension.implot.ImPlot
import imgui.extension.implot.ImPlotSpec

fun implotSpec(block: ImPlotSpec.() -> Unit) = ImPlotSpec().apply(block)

object ImPlotContext {
    fun line(name: String, data: DoubleArray, xScale: Double = 1.0, xStart: Double = 0.0, spec: ImPlotSpec? = null) {
        if (spec == null) ImPlot.plotLine(name, data, xScale, xStart)
        else ImPlot.plotLine(name, data, xScale, xStart, spec)
    }
}
