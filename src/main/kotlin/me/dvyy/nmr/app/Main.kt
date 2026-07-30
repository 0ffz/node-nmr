package me.dvyy.nmr.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.Snapshot
import imgui.ImFontConfig
import imgui.ImGui
import imgui.ImVec2
import imgui.ImVec4
import imgui.app.Application
import imgui.app.Configuration
import imgui.extension.imnodes.ImNodes
import imgui.extension.implot.ImPlot
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiConfigFlags
import imgui.flag.ImGuiDir
import imgui.flag.ImGuiStyleVar
import imgui.type.ImInt
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import me.dvyy.nmr.app.bindings.imgui.ImGuiKt
import me.dvyy.nmr.app.core.dispatchers.AppDispatchers
import me.dvyy.nmr.app.graphs.data.GraphType
import me.dvyy.nmr.app.graphs.ui.screens.Graph2DScreen
import me.dvyy.nmr.app.menubar.AppMenuBar
import me.dvyy.nmr.app.menubar.MenuViewModel
import me.dvyy.nmr.app.nodes.ui.NodeGraphViewModel
import me.dvyy.nmr.app.nodes.ui.screens.NodeListScreen
import me.dvyy.nmr.app.nodes.ui.screens.NodeScreen
import me.dvyy.nmr.common.helpers.loadFromResources
import me.dvyy.nmr.ui.graphs.GraphScreen
import java.util.*
import kotlin.concurrent.atomics.AtomicBoolean
import kotlin.concurrent.atomics.ExperimentalAtomicApi
import imgui.internal.ImGui as ImGuiInternal

@OptIn(ExperimentalAtomicApi::class)
class Main : Application() {
    val scope = CoroutineScope(Dispatchers.IO)
    val nodeGraph by lazy { NodeGraphViewModel() }
    val menuViewModel by lazy { MenuViewModel() }
    val uiScope = CoroutineScope(Dispatchers.Main)
    var plots by mutableStateOf(EnumSet.of(GraphType.FID, GraphType.FFT))

    private val applyScheduled = AtomicBoolean(false)
    private val snapshotHandle = Snapshot.registerGlobalWriteObserver {
        applyScheduled.compareAndSet(expectedValue = false, newValue = true)
    }

    override fun initImGui(config: Configuration?) {
        super.initImGui(config)
        val io = ImGui.getIO()
        io.iniFilename = null                                // We don't want to save .ini file
        io.fonts.setFreeTypeRenderer(true)
        io.fonts.addFontFromMemoryTTF(loadFromResources("/NotoSans.ttf"), 24f, ImFontConfig())
        io.fonts.addFontFromMemoryTTF(loadFromResources("/MaterialSymbolsRounded_28pt-Regular.ttf"), 28f, ImFontConfig().apply {
            mergeMode = true
            glyphOffset = ImVec2(0f, 4f)
            glyphMinAdvanceX = 28f
            glyphRanges = shortArrayOf(0xE000.toShort(), 0xF8FF.toShort(), 0)
        })
        io.fonts.build()
        ImGui.styleColorsLight()
        io.addConfigFlags(ImGuiConfigFlags.NavEnableKeyboard)  // Enable Keyboard Controls
        io.addConfigFlags(ImGuiConfigFlags.DockingEnable)      // Enable Docking
        io.addConfigFlags(ImGuiConfigFlags.ViewportsEnable)
        ImGui.pushStyleColor(ImGuiCol.Header, ImVec4(0.5f, 0.5f, 0.5f, 0.25f))
        val rounding = 4f
        ImGui.pushStyleVar(ImGuiStyleVar.WindowRounding, rounding)
        ImGui.pushStyleVar(ImGuiStyleVar.ChildRounding, rounding)
        ImGui.pushStyleVar(ImGuiStyleVar.FrameRounding, rounding)
        ImGui.pushStyleVar(ImGuiStyleVar.PopupRounding, rounding)
        ImGui.pushStyleVar(ImGuiStyleVar.ScrollbarRounding, rounding)
        ImGui.pushStyleVar(ImGuiStyleVar.GrabRounding, rounding)
        ImGui.pushStyleVar(ImGuiStyleVar.TabRounding, rounding)
        ImPlot.createContext()
        ImNodes.createContext()
        init()
    }

    fun init() {
        menuViewModel.updateColors()
    }

    fun setupDocking(dockspaceId: Int) {
        val bottom = ImInt()
        val top = ImInt()
        val bottomRight = ImInt()
        val bottomLeft = ImInt()
        ImGuiInternal.dockBuilderSplitNode(dockspaceId, ImGuiDir.Up, 0.5f, top, bottom)
        ImGuiInternal.dockBuilderSplitNode(bottom.get(), ImGuiDir.Left, 0.2f, bottomLeft, bottomRight)
        ImGuiInternal.dockBuilderDockWindow("Graphs 2D", top.get())
        ImGuiInternal.dockBuilderDockWindow("Graphs", top.get())
        ImGuiInternal.dockBuilderDockWindow("Nodes", bottomRight.get())
        ImGuiInternal.dockBuilderDockWindow("Spectra", bottomLeft.get())
        ImGuiInternal.dockBuilderFinish(dockspaceId)

    }

    override fun process() = with(ImGuiKt) {
        if (applyScheduled.compareAndSet(expectedValue = true, newValue = false)) {
            Snapshot.sendApplyNotifications()
        }
        AppDispatchers.Frontend.executeDispatchedTasks()

        val dockspaceId = ImGui.dockSpaceOverViewport()
        mainMenuBar {
            AppMenuBar(menuViewModel, nodeGraph)
        }
        if (menuViewModel.first) {
            menuViewModel.first = false
            setupDocking(dockspaceId)
        }

        withStyle(ImGuiStyleVar.WindowPadding, 0.0f, 0.0f) {
            window("Graphs") {
                GraphScreen(
                    nodeGraph.nodes, plots, onPlotsChange = {
                        plots = it
                    }
                )
            }
            window("Graphs 2D") {
                Graph2DScreen(
                    nodes = nodeGraph.nodes,
                )
            }
        }

        window("Spectra") {
            NodeListScreen()
        }
        window("Nodes") {
            NodeScreen(nodeGraph)
        }
    }
}


fun main() {
    Application.launch(Main())
}
