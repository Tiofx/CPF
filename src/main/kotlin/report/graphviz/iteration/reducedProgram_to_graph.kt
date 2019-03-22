package report.graphviz.iteration

fun ReducedProgram.Indexes.toGraph(toOperator: IntRange.() -> List<Node>): String {
    class Graph(val start: List<Node>,
                val group: List<Node>,
                val end: List<Node>) : Node {

        private val allNodes get() = start + group + end
        private val groupNode get() = NodeGroup(group)

        override fun toGraphviz(): String = """
        |digraph {
        |   ranksep = 0.3;
        |   node [shape=circle fontsize=14 fontname="Times New Roman"];
        |
        |   ${allNodes.connect()}
        |   ${groupNode.toGraphviz()}
        |}
    """.trimMargin()

        private fun List<Node>.connect(): String = map(Node::toGraphviz).reduce { acc, n -> "$acc -> $n" }
    }

    val startPart = if (hasSkipBeforeGroup) start.toOperator() + NodeSkip() else start.toOperator()

    val groupPart =
            if (hasSkipInGroup)
                groupStart.toOperator() + NodeSkip() + groupEnd.toOperator()
            else groupStart.toOperator() + groupEnd.toOperator()

    val endPart = if (hasSkipBeforeEnd) listOf(NodeSkip()) + end.toOperator() else end.toOperator()

    return Graph(startPart, groupPart, endPart).toGraphviz()
}