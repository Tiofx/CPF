package report.graphviz.iteration

interface Node {
    fun toGraphviz(): String
}

class SingleOperator(val name: String) : Node {
    override fun toGraphviz(): String = name
}

class NodeSkip : Node {
    override fun toGraphviz(): String = "$this[label='...']"
}

class NodeGroup(val nodeSequence: List<Node>) : Node {
    override fun toGraphviz(): String = """
        |subgraph cluster_group {
        |    penwidth = 2
        |    margin = 10
        |
        |   ${nodeSequence.joinToString(" ", transform = Node::toGraphviz)}
        |}
    """.trimMargin()
}