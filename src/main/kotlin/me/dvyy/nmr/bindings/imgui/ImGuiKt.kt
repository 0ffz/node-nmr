package me.dvyy.nmr.bindings.imgui

import imgui.ImGui.*
import imgui.ImVec2
import imgui.extension.implot.ImPlot
import imgui.extension.implot.ImPlotSpec

fun implotSpec(block: ImPlotSpec.() -> Unit) = ImPlotSpec().apply(block)

object ImPlotContext {
    fun line(name: String, data: DoubleArray, xScale: Double = 1.0, xStart: Double = 0.0, spec: ImPlotSpec? = null) {
        if(spec == null) ImPlot.plotLine(name, data, xScale, xStart)
        else ImPlot.plotLine(name, data, xScale, xStart, spec)
    }
}

object ImGuiKt {
    inline fun sliderFloat(label: String, value: Float, min: Float, max: Float, onChange: (Float) -> Unit) {
        val arr = floatArrayOf(value)
        if (sliderFloat(label, arr, min, max)) onChange(arr[0])
    }

    inline fun window(label: String, flags: Int = 0, content: () -> Unit) {
        if (begin(label, flags)) content()
        end()
    }

    inline fun button(label: String, onClick: () -> Unit) {
        if (button(label)) onClick()
    }

    inline fun plot(label: String, content: ImPlotContext.() -> Unit) {
        if (ImPlot.beginPlot(label)) {
            content(ImPlotContext)
        }
        ImPlot.endPlot()
    }

    inline fun subplots(label: String, rows: Int, cols: Int, size: ImVec2 = ImVec2(-1f, -1f), content: () -> Unit) {
        if (ImPlot.beginSubplots(label, rows, cols, size)) {
            content()
            ImPlot.endSubplots()
        }
    }
}