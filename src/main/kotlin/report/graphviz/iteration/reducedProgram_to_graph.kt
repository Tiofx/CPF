package report.graphviz.iteration

fun ReducedProgram.Indexes.toGraph(toOperator: IntRange.() -> List<Node>): String {
    class Graph(val start: List<Node>,
                val group: List<Node>,
                val end: List<Node>) {

        private val allNodes get() = start + group + end
        private val groupNode get() = NodeGroup(group)

        fun toGraphviz(): String = """
        |digraph {
        |   ranksep = 0.35;
        |   node [shape=circle, fontsize=14, fontname="Times New Roman", margin=".1,.01", fixedsize=true]
        |
        |   ${allNodes.declare()}
        |   ${allNodes.connect()}
        |   ${groupNode.toGraphviz()}
        |}
    """.trimMargin()

        private fun List<Node>.declare(): String = map(Node::nodeName).reduce { acc, n -> "$acc  $n" }
        private fun List<Node>.connect(): String = map(Node::toGraphviz).reduce { acc, n -> "$acc -> $n" }
    }

    fun IntRange.endExclusive(): IntRange = start until endInclusive

    val startPart =
            if (hasSkipBeforeGroup)
                start.endExclusive().toOperator() + NodeSkip()
            else
                start.toOperator()

    val groupPart =
            if (hasSkipInGroup)
                groupStart.endExclusive().toOperator() + NodeSkip() + groupEnd.toOperator()
            else
                groupStart.toOperator() + groupEnd.toOperator()

    val endPart =
            if (hasSkipBeforeEnd)
                listOf(NodeSkip()) + end.endExclusive().toOperator()
            else
                end.toOperator()

    return Graph(startPart, groupPart, endPart).toGraphviz()
}