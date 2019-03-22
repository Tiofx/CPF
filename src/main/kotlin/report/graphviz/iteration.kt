package report.graphviz
import kotlin.math.min
class ReducedProgram(val limit: Int = 15,
//                     val groupMinSize: Int = 3,
                     val startMinSize: Int = 2,
                     val endMinSize: Int = 2) {
    lateinit var iteration: CPF.Iteration

    val group get() = iteration.groupedOperators
    val program get() = iteration.program

    val startMaxSize get() = group.start
    val endMaxSize get() = program.lastIndex - group.endInclusive
    val groupMaxSize get() = group.toList().size

    val remFromStart get() = remNumber(startMinSize, startMaxSize)
    val remFromEnd get() = remNumber(endMinSize, endMaxSize)

    val groupPreferredSize get() = limit - (startMinSize + endMinSize) + remFromStart + remFromEnd

    val groupSize get() = min(groupPreferredSize, groupMaxSize)
    val startSize get() = (limit - groupSize) / 2 + (limit - groupSize) % 2
    val endSize get() = limit - (startSize + groupSize)

    val startRange get() = if (startSize > 0) 0..(startSize - 1) else IntRange.EMPTY
    val groupStartRange get() = group.start..(group.start + groupSize / 2 + groupSize % 2 - 1)
    val groupSkipAdditionalSpace get() = if (groupMaxSize - groupSize == 1) 1 else 0
    val groupEndRange get() = (group.endInclusive - groupSize / 2 + 1 + groupSkipAdditionalSpace)..group.endInclusive
    val endRange get() = if (endSize > 0) (groupEndRange.endInclusive + 1)..program.lastIndex else IntRange.EMPTY

    fun parse() = Indexes(
            startRange,
            groupStartRange,
            groupEndRange,
            endRange
    )

    data class Indexes(
            val start: IntRange,
            val groupStart: IntRange,
            val groupEnd: IntRange,
            val end: IntRange
    ) {
        val hasSkipBeforeGroup: Boolean = !start.isEmpty() && start.endInclusive + 1 == groupStart.start
        val hasSkipInGroup: Boolean = groupStart.endInclusive + 1 == groupEnd.start
        val hasSkipBeforeEnd: Boolean = !end.isEmpty() && groupEnd.endInclusive + 1 == end.start
    }

    private fun remNumber(min: Int, max: Int) = if (max < min) min - max else 0
}

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

