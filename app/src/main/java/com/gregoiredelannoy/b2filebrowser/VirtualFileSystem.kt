package com.gregoiredelannoy.b2filebrowser

class Node(
    val parent: Node?,
    val children: MutableList<Node>?,
    val name: String,
    val path: String,
    val fileDescription: FileDescription?
)

class VirtualFileSystem(input: List<FileDescription>) {
    val root = Node(
        parent = null,
        children = mutableListOf(),
        name = "/",
        path = "/",
        fileDescription = null
    )

    private fun printRecurse(node: Node, level: Int) {
        var indent = ""
        for (i in 1..level) indent += "  "
        println(indent + node.name)
        if (node.children != null) {
            node.children.forEach {
                printRecurse(it, level + 1)
            }
        }
    }

    fun print() {
        printRecurse(this.root, 0)
    }


    // Create only intermediate nodes, folders, which are not included in the b2 responses. Files (leaves) will not be created here
    private fun getCreateFolderChildNode(workingNode: Node, folder: String): Node {
        if (workingNode.children == null) {
            throw Exception("Error, workingDirectory is not a directory!")
        } else {
            val existing = workingNode.children.find { it.name == folder }
            return if (existing == null) {
                val new = Node(
                    parent = workingNode,
                    children = mutableListOf(),
                    name = folder,
                    path = "${workingNode.path}$folder/",
                    fileDescription = null
                )
                workingNode.children.add(new)
                new
            } else {
                existing
            }
        }
    }

    private fun insertInTree(input: FileDescription) {
        if (input.fileName == null) {
            throw Exception("null fileName, cannot split, cannot work.")
        }

        val splatPath = input.fileName.split('/')

        var workingNode = this.root
        for (i in 0 until splatPath.size - 1) {
            workingNode = getCreateFolderChildNode(workingNode, splatPath[i])
        }

        val leaf = Node(
            parent = workingNode,
            children = null,
            name = splatPath.last(),
            path = input.fileName,
            fileDescription = input
        )

        if (workingNode.children == null) {
            throw Exception("Trying to insert file in tree, but workingNode's children list is null.")
        } else {
            workingNode.children!!.add(leaf)
        }
    }

    init {
        input.forEach {
            insertInTree(it)
        }
    }
}