package me.dvyy.nmr.ui.nodes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import imgui.extension.imnodes.ImNodes
import io.github.vinceglb.filekit.FileKit
import io.github.vinceglb.filekit.dialogs.FileKitDialogSettings
import io.github.vinceglb.filekit.dialogs.openFilePicker
import io.github.vinceglb.filekit.dialogs.openFileSaver
import io.github.vinceglb.filekit.readBytes
import io.github.vinceglb.filekit.writeString
import kotlinx.collections.immutable.PersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import me.dvyy.nmr.nodes.format.Project
import me.dvyy.nmr.nodes.format.ProjectConverter

class NodeGraphViewModel(
    val repository: NodeRepository = NodeRepository()
) {
    init {
        ImNodes.createContext()
    }

    val editorContext = ImNodes.editorContextCreate()
    val nodes: PersistentList<Node> get() = repository.nodes
    val links: Set<NodeLink> get() = repository.links
    var selectedNode by mutableStateOf(-1)
    val scope = CoroutineScope(Dispatchers.IO)

    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    fun link(from: OutputAttribute<*>, into: InputAttribute<*>) {
        repository.link(from, into)
    }

    fun unlink(linkId: Int) {
        repository.unlink(linkId)
    }

    fun removeNode(id: Int) {
        repository.removeNode(id)
    }

    fun findAttribute(id: Int): Attribute<*>? {
        return repository.findAttribute(id)
    }

    fun <T : Node> addNode(node: T): T {
        return repository.addNode(node)
    }

    fun clearProject() {
        repository.clear()
    }

    fun saveProject() {
        scope.launch {
            val project = ProjectConverter.exportProject(repository)
            val jsonString = json.encodeToString(Project.serializer(), project)
            val file = FileKit.openFileSaver("project", defaultExtension = "json") ?: return@launch
            file.writeString(jsonString)
        }
    }

    fun loadProject() {
        scope.launch {
            val file = FileKit.openFilePicker(dialogSettings = FileKitDialogSettings(title = "Open Project File")) ?: return@launch
            val text = file.readBytes().decodeToString()
            val project = json.decodeFromString(Project.serializer(), text)
            ProjectConverter.importProject(repository, project)
        }
    }

    companion object {
        var id = 1
        fun nextId(): Int = id++
    }
}
