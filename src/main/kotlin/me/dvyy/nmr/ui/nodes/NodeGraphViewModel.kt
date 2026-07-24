package me.dvyy.nmr.ui.nodes

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import imgui.extension.imnodes.ImNodes
import kotlinx.collections.immutable.PersistentList
import kotlinx.collections.immutable.persistentListOf
import me.dvyy.nmr.parsing.BrukerDataset
import me.dvyy.nmr.ui.nodes.inputs.DatasetNode
import me.dvyy.nmr.ui.nodes.outputs.GraphNode

class NodeGraphViewModel() {

    companion object {
        var id = 1
        fun nextId(): Int = id++

    }

    init {
        ImNodes.createContext()
    }

    val editorContext = ImNodes.editorContextCreate()

    var nodes by mutableStateOf<PersistentList<Node>>(persistentListOf())
    private val _links = mutableSetOf<NodeLink>()
    private val linkIds = mutableListOf<Int>()
    val links: Set<NodeLink> = _links
    var selectedNode by mutableStateOf(-1)
    init {

//        val dataset = addNode(DatasetNode(dataset))
//        val apod = addNode(ApodizationTransformation())
//        addNode(WaveletTransformation())
//        val graph = addNode(GraphNode())
//        link(dataset.output, apod.inputRef)
//        link(dataset.output, graph.input)
    }

    /**
     * Links the output of an [from] node to the input of an [into] node
     */
    fun link(from: OutputAttribute<*>, into: InputAttribute<*>) {
        if (from.pipeInto(into)) {
            _links.removeIf { it.into.id == into.id }
            _links.add(NodeLink(id++, from, into))
        }
    }

    fun unlink(linkId: Int) {
        val link = _links.find { it.id == linkId } ?: return
        _links.remove(link)
        link.into.removePipe()
    }

    fun removeNode(id: Int) {
        val index = nodes.indexOfFirst { it.id == id }.takeIf { it != -1 } ?: return
        val node = nodes[index]
        nodes = nodes.removeAt(index)
        node.attributes.forEach { attr ->
            val id = attr.id
            _links.toList().forEach {
                if (it.from.id == id || it.into.id == id) {
                    unlink(it.id)
                }
            }

        }
    }

    fun findAttribute(id: Int): Attribute<*>? {
        nodes.forEach {
            it.attributes.forEach { attr -> if (attr.id == id) return attr }
        }
        return null
    }
//    fun loadDataset(dataset: SignalProviding, name: String): Node {
//        val node = Node(id++, name, dataset, outputId = id++, inputId = null)
//        nodes = nodes.add(node)
//        return node
//    }

    fun <T : Node> addNode(node: T): T {
        nodes = nodes.add(node)
        return node
    }
}

