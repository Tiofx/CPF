package algorithm


private val exampleControlGraph = ControlGraph(
    listOf(
        1 to 2,
        1 to 3,
        1 to 5,
        2 to 4,
        3 to 5,
        3 to 7,
        3 to 8,
        4 to 6,
        4 to 10,
        5 to 6,
        5 to 9,
        6 to 9,
//        7 to 6,
        7 to 10,
        8 to 10
    )
)

private val myControlGraph = ControlGraph(
    listOf(
        1 to 2,
        1 to 3,
        2 to 7,
        3 to 4,
        3 to 5,
        4 to 7,
        5 to 6,
        6 to 7,
        7 to 8,
        7 to 9,
        8 to 13,
        9 to 10,
        9 to 11,
        10 to 13,
        11 to 12,
        12 to 13
    )
)
val inputControlGraph = exampleControlGraph

class ControlGraph(
    adjacencyList: List<Pair<Int, Int>>,
    val size: Int = adjacencyList.flatMap { listOf(it.first, it.second) }.max()!!
) {
    private val adjacencyMatrix = List(size + 1) { MutableList(size + 1) { false } }.apply {
        adjacencyList.forEach { (from, to) ->
            this[from][to] = true
        }
    }

    fun G(operator: Int) = type(operator)
    fun type(operator: Int) = (1..size).filter { adjacencyMatrix[operator][it] }.toSet()

    fun invG(operator: Int) = prototype(operator)
    fun prototype(operator: Int) = (1..size).filter { adjacencyMatrix[it][operator] }.toSet()
}

typealias VertexSet = Set<Int>

abstract class HasControlGraph(protected val controlGraph: ControlGraph) {
    protected val VertexSet.G get() = type
    protected val VertexSet.type get() = flatMap { controlGraph.type(it) }.toSet()

    protected val VertexSet.invG get() = prototype
    protected val VertexSet.prototype get() = flatMap { controlGraph.prototype(it) }.toSet()

}


operator fun <T> Set<T>.contains(subSet: Set<T>) =
    if (isNotEmpty()) subSet.all { it in this }
    else isEmpty() && subSet.isEmpty()