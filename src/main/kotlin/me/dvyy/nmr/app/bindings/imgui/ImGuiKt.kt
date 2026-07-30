package me.dvyy.nmr.app.bindings.imgui

import imgui.ImGui
import imgui.ImGui.*
import imgui.ImVec2
import imgui.extension.imnodes.ImNodes
import imgui.extension.implot.ImPlot
import imgui.extension.implot.flag.ImPlotFlags
import imgui.flag.ImGuiTreeNodeFlags
import imgui.type.ImBoolean
import imgui.type.ImDouble
import java.awt.Color
import kotlin.math.abs

object ImGuiKt {
    inline fun text(text: String) {
        ImGui.text(text)
    }

    inline fun sliderFloat(label: String, value: Float, min: Float, max: Float, format: String = "%.6f", onChange: (Float) -> Unit) {
        val arr = floatArrayOf(value)
        if (sliderFloat(label, arr, min, max, format)) onChange(arr[0])
    }

    inline fun dragFloat(
        label: String,
        value: Float,
        min: Float = 1f,
        max: Float = 0f,
        scaleNearZero: Boolean = true,
        onChange: (Float) -> Unit,
    ) {
        val arr = floatArrayOf(value)
        val speed = when {
            !scaleNearZero -> 0.1f
            abs(value) <= 0.01f -> 0.00001f
            abs(value) <= 0.1f -> 0.0001f
            abs(value) <= 1f -> 0.001f
            abs(value) <= 10f -> 0.01f
            else -> 0.1f
        }
        val format = when {
            abs(value) <= 0.01f -> "%.5f"
            abs(value) <= 0.1f -> "%.4f"
            abs(value) <= 1f -> "%.3f"
            abs(value) <= 10f -> "%.2f"
            else -> "%.1f"
        }
        if (dragFloat(label, arr, speed, min, max, format)) onChange(arr[0])
    }

    inline fun dragDouble(
        label: String,
        value: Double,
        min: Double = 1.0,
        max: Double = 0.0,
        scaleNearZero: Boolean = true,
        onChange: (Double) -> Unit,
    ) {
        dragFloat(label, value.toFloat(), min.toFloat(), max.toFloat(), scaleNearZero, onChange = { onChange(it.toDouble()) })
    }

    inline fun sliderDouble(label: String, value: Double, min: Double, max: Double, format: String = "%.6f", onChange: (Double) -> Unit) {
        sliderFloat(label, value.toFloat(), min.toFloat(), max.toFloat(), format) { onChange(it.toDouble()) }
    }

    inline fun inputDouble(label: String, value: Double, step: Double = 0.0, stepFast: Double = 0.0, format: String = "%.6f", onChange: (Double) -> Unit) {
        val double = ImDouble(value)
        if (inputDouble(label, double, step, stepFast, format)) onChange(double.get())
    }

    inline fun sliderInt(label: String, value: Int, min: Int, max: Int, onChange: (Int) -> Unit) {
        val arr = intArrayOf(value)
        if (sliderInt(label, arr, min, max)) onChange(arr[0])
    }

    inline fun checkbox(label: String, value: Boolean, onChange: (Boolean) -> Unit) {
        val bool = ImBoolean(value)
        if (checkbox(label, bool)) onChange(bool.get())
    }

    inline fun colorEdit4(
        label: String,
        color: Color,
        onChange: (Color) -> Unit,
        flags: Int = 0,
    ) {
        val arr = floatArrayOf(
            color.red / 255f,
            color.green / 255f,
            color.blue / 255f,
            color.alpha / 255f
        )
        if (colorEdit4(label, arr, flags)) {
            onChange(
                Color(
                    (arr[0] * 255).toInt().coerceIn(0, 255),
                    (arr[1] * 255).toInt().coerceIn(0, 255),
                    (arr[2] * 255).toInt().coerceIn(0, 255),
                    (arr[3] * 255).toInt().coerceIn(0, 255)
                )
            )
        }
    }

    inline fun window(label: String, flags: Int = 0, content: () -> Unit) {
        if (begin(label, flags)) content()
        end()
    }

    inline fun collapsingHeader(
        label: String,
        defaultOpen: Boolean = false,
        flags: Int = 0,
        content: () -> Unit,
    ) {
        if (collapsingHeader(label, flags or (if (defaultOpen) ImGuiTreeNodeFlags.DefaultOpen else 0))) content()
    }

    inline fun treeNode(
        label: String,
        flags: Int = 0,
        defaultOpen: Boolean = false,
        header: () -> Unit = {},
        content: () -> Unit,
    ) {
        val open = treeNodeEx(label, flags or (if (defaultOpen) ImGuiTreeNodeFlags.DefaultOpen else 0))
        header()
        if(open) {
            content()
            treePop()
        }
    }

    inline fun button(label: String, onClick: () -> Unit) {
        if (button(label)) onClick()
    }

    inline fun withStyle(styleVar: Int, x: Float, y: Float, content: () -> Unit) {
        pushStyleVar(styleVar, x, y)
        content()
        popStyleVar()
    }

    inline fun withStyle(styleVar: Int, value: Float, content: () -> Unit) {
        pushStyleVar(styleVar, value)
        content()
        popStyleVar()
    }

    inline fun node(id: Int, content: ImNodeContext.() -> Unit) {
        ImNodes.beginNode(id)
        content(ImNodeContext)
        ImNodes.endNode()
    }

    inline fun plot(
        label: String,
        size: ImVec2? = null,
        flags: Int = 0,
        content: ImPlotContext.() -> Unit,
    ) {
        if (if (size == null) ImPlot.beginPlot(label, flags) else ImPlot.beginPlot(label, size, flags)) {
            content(ImPlotContext)
            ImPlot.endPlot()
        }
    }

    inline fun mainMenuBar(content: () -> Unit) {
        if (beginMainMenuBar()) {
            content()
            endMainMenuBar()
        }
    }

    inline fun menu(name: String, content: () -> Unit) {
        if (beginMenu(name)) {
            content()
            endMenu()
        }
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

    inline fun withItemWidth(width: Float, content: () -> Unit) {
        pushItemWidth(width)
        content()
        popItemWidth()
    }
}
