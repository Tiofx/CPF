package algorithm

class RelationsMatrix(impl: Relations) : Relations by impl {
    val strongDependencyMatrix: List<List<Boolean>> by lazy { relationMatrix(this::isStrongDependency) }
    val weekDependencyMatrix: List<List<Boolean>>  by lazy { relationMatrix(this::isWeekDependency) }

    val weekIndependencyMatrix: List<List<Boolean>>  by lazy {
        relationMatrix(this::isWeekIndependency)
            .apply { (0..lastIndex).forEach { this[it][it] = true } }
            .apply {
                for (i in 0..lastIndex)
                    for (j in 0..(i - 1))
                        this[i][j] = this[j][i]
            }

    }
    val strongIndependencyMatrix: List<List<Boolean>> by lazy {
        relationMatrix(this::isStrongIndependency)
            .apply { (0..lastIndex).forEach { this[it][it] = true } }
            .apply {
                for (i in 0..lastIndex)
                    for (j in 0..(i - 1))
                        this[i][j] = this[j][i]
            }
    }

    private fun relationMatrix(f: (Int, Int) -> Boolean) =
        (0..lastIndex).map { i ->
            val row = MutableList(size) { false }
            (i + 1..lastIndex).map { j ->
                row[j] = f(i, j)
            }
            row
        }
}

class CashedProgram(operators: List<IOperator>) : Program(operators) {
    override fun isStrongDependency(i: Int, j: Int): Boolean {
        if (strongDepMatrix[i][j] == null) {
            strongDepMatrix[i][j] = super.isStrongDependency(i, j)
        }

        return strongDepMatrix[i][j]!!
    }

    private val strongDepMatrix: List<MutableList<Boolean?>> = List(size) { MutableList<Boolean?>(size) { null } }


    override fun isWeekDependency(i: Int, j: Int): Boolean {
        if (weekDependencyMatrix[i][j] == null) {
            weekDependencyMatrix[i][j] = super.isWeekDependency(i, j)
        }

        return weekDependencyMatrix[i][j]!!
    }

    private val weekDependencyMatrix: List<MutableList<Boolean?>> = List(size) { MutableList<Boolean?>(size) { null } }

    override fun isWeekIndependency(i: Int, j: Int): Boolean {
        if (weekIndependencyMatrix[i][j] == null) {
            weekIndependencyMatrix[i][j] = super.isWeekIndependency(i, j)
        }

        return weekIndependencyMatrix[i][j]!!
    }

    private val weekIndependencyMatrix: List<MutableList<Boolean?>> =
        List(size) { MutableList<Boolean?>(size) { null } }


    override fun isStrongIndependency(i: Int, j: Int): Boolean {
        if (strongIndependencyMatrix[i][j] == null) {
            strongIndependencyMatrix[i][j] = super.isStrongIndependency(i, j)
        }

        return strongIndependencyMatrix[i][j]!!
    }

    private val strongIndependencyMatrix: List<MutableList<Boolean?>> =
        List(size) { MutableList<Boolean?>(size) { null } }
}

operator fun <E> Set<E>.contains(s: Set<E>) = s.all { it in this }