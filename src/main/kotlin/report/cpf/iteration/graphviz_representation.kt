package report.cpf.iteration

interface Node {
    val nodeName: String
    fun toGraphviz(): String
}

class SingleOperator(val name: String) : Node {
    override val nodeName: String get() = "\"$name\""

    override fun toGraphviz(): String = "\"$name\""
}

class NodeSkip : Node {
    override val nodeName: String get() = "\"${hashCode()}\"[label=\"...\", color=invis]"

    override fun toGraphviz(): String = "\"${hashCode()}\""
}

class NodeGroup(val groupName:String, val nodeSequence: List<Node>) : Node {
    override val nodeName: String get() = TODO("not implemented")

    override fun toGraphviz(): String = """
        |subgraph cluster_group {
        |    penwidth = 2
        |    margin = 10
        |    label = "$groupName"
        |
        |   ${nodeSequence.joinToString(" ", transform = Node::nodeName)}
        |}
    """.trimMargin()
}