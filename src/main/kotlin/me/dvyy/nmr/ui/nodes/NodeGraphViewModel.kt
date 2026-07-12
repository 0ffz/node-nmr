package me.dvyy.nmr.ui.nodes

import imgui.extension.imnodes.ImNodes
import me.dvyy.nmr.parsing.BrukerDataset

class NodeGraphViewModel(
    val dataset: BrukerDataset,
) {
    init {
        ImNodes.createContext()
    }
    val editorContext = ImNodes.editorContextCreate()

    var id = 1
    val nodes = mutableListOf(
        Node.Process(id++, "Apodization", ApodizationTransformation(), id++, id++),
        Node.Process(id++, "Wavelet", WaveletTransformation(), id++, id++),
        Node.Input(id++, "Dataset", dataset, id++)
    )
    private val _links = mutableMapOf<Int, Int>()
    val links: Map<Int, Int> = _links

    /**
     * Links the output of an [input] node to the input of an [output] node
     */
    fun link(input: Node, output: Node.Process) {
        _links[input.outputId] = output.inputId
        input.signalStep.pipeInto(output.signalStep)
    }

    fun unlink(linkId: Int) {
        val output = _links[linkId] ?: return
        _links.remove(linkId)
        (nodeForAttribute(output) as? Node.Process)?.signalStep?.removePipe()
    }

    fun nodeForAttribute(id: Int): Node? {
        return nodes.find { it.outputId == id || (it as? Node.Process)?.inputId == id}
    }

    fun loadDataset(dataset: BrukerDataset, name: String) {
        nodes.add(Node.Input(id++, name, dataset, id++))
    }

}
