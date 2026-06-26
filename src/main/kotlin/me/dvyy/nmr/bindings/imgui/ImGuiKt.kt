package me.dvyy.nmr.bindings.imgui

import imgui.ImGui.*
import imgui.ImVec2
import imgui.extension.implot.ImPlot
import imgui.extension.implot.ImPlotSpec
import imgui.extension.implot.flag.ImPlotFlags

fun implotSpec(block: ImPlotSpec.() -> Unit) = ImPlotSpec().apply(block)

object ImPlotContext {
    fun line(name: String, data: DoubleArray, xScale: Double = 1.0, xStart: Double = 0.0, spec: ImPlotSpec? = null) {
        if(spec == null) ImPlot.plotLine(name, data, xScale, xStart)
        else ImPlot.plotLine(name, data, xScale, xStart, spec)
    }
}

object ImGuiKt {
    inline fun sliderFloat(label: String, value: Float, min: Float, max: Float, format: String = "%.6f", onChange: (Float) -> Unit) {
        val arr = floatArrayOf(value)
        if (sliderFloat(label, arr, min, max, format)) onChange(arr[0])
    }

    inline fun sliderDouble(label: String, value: Double, min: Double, max: Double, format: String = "%.6f", onChange: (Double) -> Unit) {
        sliderFloat(label, value.toFloat(), min.toFloat(), max.toFloat(), format) { onChange(it.toDouble()) }
    }

    inline fun sliderInt(label: String, value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
        val arr = intArrayOf(value)
        if (sliderInt(label, arr, min, max)) onChange(arr[0])
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

    inline fun subplots(
        label: String,
        rows: Int,
        cols: Int,
        size: ImVec2 = ImVec2(-1f, -1f),
        flags: Int = ImPlotFlags.None,
        content: () -> Unit,
    ) {
        if (ImPlot.beginSubplots(label, rows, cols, size, flags)) {
            content()
            ImPlot.endSubplots()
        }
    }
}