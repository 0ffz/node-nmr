package me.dvyy.nmr.ui.processing

import me.dvyy.nmr.bindings.imgui.ImGuiKt
import me.dvyy.nmr.ui.SpectrumViewModel

fun ImGuiKt.SingularValuesGraph(state: SpectrumViewModel) {
    plot("Singular values") {
        state.svdResults.forEachIndexed { index, values ->
            line("#$index", values)
        }
    }
}