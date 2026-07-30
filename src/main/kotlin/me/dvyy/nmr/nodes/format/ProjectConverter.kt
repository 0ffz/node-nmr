package me.dvyy.nmr.nodes.format

import imgui.ImVec2
import imgui.extension.imnodes.ImNodes
import me.dvyy.nmr.io.project.LinkModel
import me.dvyy.nmr.io.project.NodeModel
import me.dvyy.nmr.io.project.Project
import me.dvyy.nmr.io.project.Vec2Model
import me.dvyy.nmr.ui.nodes.InputAttribute
import me.dvyy.nmr.ui.nodes.Node
import me.dvyy.nmr.ui.nodes.NodeRegistry
import me.dvyy.nmr.ui.nodes.NodeRepository
import me.dvyy.nmr.ui.nodes.OutputAttribute

object ProjectConverter {
    fun exportProject(repository: NodeRepository): Project {
        val nodeModels = repository.nodes.map { node ->
            val pos = ImVec2()
            ImNodes.getNodeGridSpacePos(pos, node.id)
            NodeModel(
                id = node.id,
                type = node.name,
                position = Vec2Model(pos.x, pos.y),
                params = node.exportParameters()
            )
        }

        val linkModels = repository.links.map { link ->
            val fromNode = repository.nodes.find { n -> n.attributes.contains(link.from) }
            val toNode = repository.nodes.find { n -> n.attributes.contains(link.into) }
            LinkModel(
                id = link.id,
                fromNode = (fromNode?.id ?: -1),
                fromAttribute = link.from.localId,
                toNode = (toNode?.id ?: -1),
                toAttribute = link.into.localId,
            )
        }

        return Project(version = 1, nodes = nodeModels, links = linkModels)
    }

    fun importProject(repository: NodeRepository, project: Project) {
        repository.clear()

        val oldToNewNodeMap = mutableMapOf<Int, Node>()

        project.nodes.forEach { nodeModel ->
            val node = NodeRegistry.create(nodeModel.type) ?: return@forEach
            node.importParameters(nodeModel.params)
            repository.addNode(node)
            oldToNewNodeMap[nodeModel.id] = node

            ImNodes.setNodeGridSpacePos(node.id, nodeModel.position.x, nodeModel.position.y)
        }

        project.links.forEach { linkModel ->
            val fromNodeId = linkModel.fromNode
            val toNodeId = linkModel.toNode
            val fromNode = oldToNewNodeMap[fromNodeId] ?: return@forEach
            val toNode = oldToNewNodeMap[toNodeId] ?: return@forEach

            val fromAttr = fromNode.attributes[linkModel.fromAttribute] as? OutputAttribute<*>
            val toAttr = toNode.attributes[linkModel.toAttribute] as? InputAttribute<*>

            if (fromAttr != null && toAttr != null) {
                repository.link(fromAttr, toAttr)
            }
        }
    }
}
