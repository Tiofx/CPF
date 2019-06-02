package algorithm


class DirectConnectionsMatrix(val controlGraph: ControlGraph) : Matrix<GraphElement> {
    override val size get() = controlGraph.size

    private val matrix by lazy<List<List<GraphElement>>> {
        List(size + 1) { MutableList<GraphElement>(size + 1) { NoConnection } }
            .apply {
                controlGraph.adjacencyList.forEach {
                    //                    this[it.from][it.to] = it.connection
                    this[it.to][it.from] = it.connection
                }
            }
    }

    override operator fun get(i: Int, j: Int) = matrix[i][j]
}

class TransitiveConnectionMatrix(val s: DirectConnectionsMatrix) : Matrix<GraphElement> {
    override val size get() = s.size

    private val matrix by lazy<List<List<GraphElement>>> {
        val st = List(size + 1) { i -> MutableList(size + 1) { j -> s[i, j] } }
        (1..size).map { j ->
            (1..size).map { i ->
                if (st[j][i] != NoConnection) {
                    (1..size).map { k ->
                        if (st[k][j] != NoConnection) {
                            st[k][i] = (st[j][i] conjunction st[k][j]) disjunction st[k][i]
//                            st[k][i] = (s[j,i] conjunction s[k,j]) disjunction s[k,i]
                        }
                    }
                }
            }
        }

        st
    }

    override operator fun get(i: Int, j: Int) = matrix[i][j]
}

class MLO(val s: DirectConnectionsMatrix) {
    val list: List<Int> = {
        val result = mutableListOf<Int>()

        for (i in 1..s.size)
            for (p in 1..s.size)
                for (q in p + 1..s.size)
                    if (s[p, i].isLogical && s[q, i].isLogical)
                        result += i

        result.distinct()
    }()
}

class LogicalIncompatibilityMatrix(val s: DirectConnectionsMatrix, val mlo: MLO) : Matrix<Boolean> {
    override val size get() = s.size

    private val matrix by lazy<List<List<Boolean>>> {
        val l = List(size + 1) { i -> MutableList(size + 1) { j -> false } }

        mlo.list.forEach { i_s ->
            val mi = mutableListOf<Int>()

            (1..size).forEach { j ->
                val el = s[j, i_s]
                if (el is LogicConnection && el.from == i_s) mi += j
            }

            mi.forEachIndexed { i, p ->
                mi.drop(i + 1).forEachIndexed { j, q ->
                    l[p][q] = true
                    l[q][p] = true
                }
            }
        }

        l
    }

    override operator fun get(i: Int, j: Int) = matrix[i][j]
}

class TransitiveLogicalIncompatibilityMatrix(val st: TransitiveConnectionMatrix, val l: LogicalIncompatibilityMatrix) :
    Matrix<Boolean> {
    override val size get() = st.size

    private val matrix by lazy<List<List<Boolean>>> {
        val lt = List(size + 1) { i -> MutableList(size + 1) { j -> false } }

        (1..size).forEach { k ->
            val Ck = C(k)
            val setOfB = Ck.map { B(it) }
            val Ak = setOfB.fold(emptySet<Int>()) { acc, s -> acc union s }

            Ak.forEach { j ->
                lt[k][j] = true
                lt[j][k] = true
            }
        }

        lt
    }

    private fun B(i: Int) = (1..size)
        .map { l[i, it] }
        .withIndex()
        .filter { it.value }
        .map { it.index + 1 }
        .toSet()

    private fun C(i: Int) = (1..size)
        .map { st[it, i] }
        .withIndex()
        .filterNot { it.value is NoConnection }
        .map { it.index + 1 }
        .toSet()

    override operator fun get(i: Int, j: Int) = matrix[i][j]
}

class IndependecyMatrix(val st: TransitiveConnectionMatrix, val lt: TransitiveLogicalIncompatibilityMatrix) :
    Matrix<Boolean> {
    override val size get() = st.size

    private val matrix by lazy<List<List<Boolean>>> {
        val m = List(size + 1) { i ->
            MutableList(size + 1) { j ->
                st[i, j] !is NoConnection || st[j, i] !is NoConnection || lt[i, j]
            }
        }

        m
    }

    override operator fun get(i: Int, j: Int) = matrix[i][j]
}

class CompleteMutuallyIndependentOperators(val m: IndependecyMatrix) {
    val size get() = m.size

    val list by lazy {
        (1..size).flatMap { i -> completeMIO(listOf(i)) }.run {
            filterNot { cmio -> filter { it != cmio }.any { cmio in it } }
        }
    }

    private fun completeMIO(indexes: List<Int>): List<Set<Int>> = (indexes.last() + 1..size)
        .filterNot { rowDisjunction(indexes)[it] }
        .flatMap { j -> completeMIO(indexes + j) }
        .let { if (it.isEmpty()) listOf(indexes.toSet()) else it }

    private fun rowDisjunction(i: List<Int>): List<Boolean> = List(size + 1) { col ->
        if (col == 0) return@List false

        i.map { row -> m[row, col] }.reduce { acc, b -> acc || b }
    }

}