package report.cpf.iteration

fun ReducedProgram.Indexes.toGraph(iterationNumber: Int, groupName: String, toOperator: IntRange.() -> List<Node>): String {
    class Graph(val start: List<Node>,
                val group: List<Node>,
                val afterGroup: List<Node>,
                val end: List<Node>) {

        private val allNodes get() = start + group + afterGroup + end
        private val groupNode get() = NodeGroup(groupName, group)

        fun toGraphviz(): String = """
        |digraph {
        |   ranksep = 0.2;
        |   node [shape=circle, fontsize=14, fontname="Times New Roman", margin=".1,.01", fixedsize=true]
        |   edge[style = invis]
        |   margin = 0
        |
        |   subgraph cluster_iteration {
        |       label = $iterationNumber
        |       margin = 5
        |
        |       ${allNodes.declare()}
        |       ${allNodes.connect()}
        |       ${groupNode.toGraphviz()}
        |   }
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

    val REM_FOR_END = 2
    val afterGroup =
            if (hasSkipBeforeEnd)
                (groupEnd.endInclusive + 1)..(groupEnd.endInclusive + 1 + end.toList().size - REM_FOR_END - 1)
            else
                IntRange.EMPTY

    val endGroup =
            if (hasSkipBeforeEnd)
                (end.endInclusive - REM_FOR_END + 1)..end.endInclusive
            else end

    val afterGroupPart = afterGroup.toOperator()

    val endPart =
            if (hasSkipBeforeEnd)
                listOf(NodeSkip()) + endGroup.endExclusive().toOperator()
            else
                endGroup.toOperator()

    return Graph(startPart, groupPart, afterGroupPart, endPart).toGraphviz()
}