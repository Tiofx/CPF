package algorithm

import kotlin.collections.List


sealed class GraphElement

object NoConnection : GraphElement()

object InformationConnection : GraphElement()

class LogicConnection(val from: Int, val connectionNumber: Int) : GraphElement() {
    val elementConnection: String
        get() = "$from.$connectionNumber"
}

class LogicComposition(val list: List<LogicConnection>) : GraphElement() {
    val content get() = list.joinToString("_") { it.elementConnection }
}

infix fun GraphElement.disjunction(snd: GraphElement) = when (this) {
    NoConnection -> snd
    InformationConnection -> if (snd is LogicConnection || snd is LogicComposition) snd else this

    is LogicConnection -> when (snd) {
        is LogicConnection -> LogicComposition(listOf(this, snd))
        is LogicComposition -> LogicComposition(listOf(this) + snd.list)
        else -> this
    }

    is LogicComposition -> when (snd) {
        is LogicConnection -> LogicComposition(list + snd)
        is LogicComposition -> LogicComposition(list + snd.list)
        else -> this
    }
}

infix fun GraphElement.conjunction(snd: GraphElement): GraphElement = when {
    this is NoConnection || snd is NoConnection -> NoConnection
    this is InformationConnection && snd is InformationConnection -> InformationConnection
    this is InformationConnection -> snd
    snd is InformationConnection -> this

    this is LogicComposition && snd is LogicComposition -> LogicComposition(list + snd.list)
    this is LogicComposition && snd is LogicConnection -> LogicComposition(list + snd)
    this is LogicConnection && snd is LogicConnection -> LogicComposition(listOf(this, snd))
    this is LogicConnection && snd is LogicComposition -> LogicComposition(listOf(this) + snd.list)

    else -> throw Exception("unknown conjunction case")
}

val GraphElement.isLogical get() = this is LogicConnection || this is LogicComposition


data class Edge(val from: Int, val to: Int, val connection: GraphElement = InformationConnection) {
    companion object {
        fun logicConnection(from: Int, to: Int, connectionNumber: Int) =
            Edge(from, to, LogicConnection(from, connectionNumber))
    }
}


val inputInfoLogicalGraph = ControlGraph(
    listOf(
        Edge.logicConnection(1, 2, 1),
        Edge.logicConnection(1, 3, 2),
        Edge.logicConnection(3, 4, 1),
        Edge.logicConnection(3, 5, 2),
        Edge.logicConnection(5, 6, 1),

        Edge.logicConnection(7, 8, 1),
        Edge.logicConnection(7, 9, 2),
        Edge.logicConnection(9, 10, 1),
        Edge.logicConnection(9, 11, 2),
        Edge.logicConnection(11, 12, 1),

        Edge(2, 13),
        Edge(4, 13),
        Edge(6, 13),
        Edge(8, 13),
        Edge(10, 13),
        Edge(12, 13)
    )
)

class ControlGraph(
    val adjacencyList: List<Edge>,
    val size: Int = adjacencyList.flatMap { listOf(it.from, it.to) }.max()!!
)


interface Matrix<T> {
    val size: Int
    operator fun get(i: Int, j: Int): T
}





operator fun <T> Set<T>.contains(subSet: Set<T>) =
    if (isNotEmpty()) subSet.all { it in this }
    else isEmpty() && subSet.isEmpty()