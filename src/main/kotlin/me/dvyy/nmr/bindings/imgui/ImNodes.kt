package me.dvyy.nmr.bindings.imgui

import imgui.extension.imnodes.ImNodes
import imgui.extension.imnodes.flag.ImNodesPinShape

object ImNodeContext {
    inline fun inputAttribute(id: Int, shape: Int = ImNodesPinShape.CircleFilled, content: () -> Unit) {
        ImNodes.beginInputAttribute(id, shape)
        content()
        ImNodes.endInputAttribute()
    }

    inline fun outputAttribute(id: Int, shape: Int = ImNodesPinShape.CircleFilled, content: () -> Unit) {
        ImNodes.beginOutputAttribute(id, shape)
        content()
        ImNodes.endOutputAttribute()
    }

    inline fun nodeTitleBar(content: () -> Unit) {
        ImNodes.beginNodeTitleBar()
        content()
        ImNodes.endNodeTitleBar()
    }
}