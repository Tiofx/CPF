package algorithm

class CPF(val init: Program) {
    private val cpfChecker = CPFChecker(init)
    private val parallelGroupOperatorFormatter = ParallelGroupOperatorFormatter(init)
    private val sequentialGroupOperatorFormatter = SequentialGroupOperatorFormatter(init)

    fun form(): List<Iteration> {
        val result = mutableListOf<Iteration>()
        var currentProgram = init
        var parallelIteration = true
        var i = 0

        while (currentProgram.size > 1) {
            setUp(currentProgram)

            var currentInfo: Iteration?

            val parallelResult = parallelGroupOperatorFormatter.analysis
            val parallelChain = parallelResult.maxChain
            val sequentialChain = sequentialGroupOperatorFormatter.maxChain

            parallelIteration = when {
                sequentialChain == null && parallelChain == null -> throw Exception("problem with CPF algorithm\n no next group chain")
                sequentialChain == null -> true
                parallelChain == null -> false
                else -> parallelChain.first <= sequentialChain.first
            }

            currentInfo = Iteration(
                            i,
                            currentProgram,
                            parallelIteration,
                            cpfChecker.CPFCheck(),
                            parallelResult)

            currentInfo = if (parallelIteration) currentInfo else currentInfo.copy(sequentialCheck = sequentialChain)

            currentProgram = if (parallelIteration)
                parallelGroupOperatorFormatter.transform(parallelResult)
            else
                sequentialGroupOperatorFormatter.transform(sequentialChain!!)

            result.add(currentInfo)
            i++
        }

        result.add(Iteration(i, currentProgram, parallelIteration, emptyList()))

        return result
    }

    private fun setUp(program: Program) {
        cpfChecker.program = program
        parallelGroupOperatorFormatter.program = program
        sequentialGroupOperatorFormatter.program = program
    }

    data class Iteration(
            val number: Int,
            val program: Program,
            val isParallel: Boolean,
            val cpfCheck: List<CPFChecker.Result>,
            val parallelCheck: ParallelGroupOperatorFormatter.Result? = null,
            val sequentialCheck: IntRange? = null
    ) {
        val allRelationsMatrices = RelationsMatrix(program)
        val groupedOperators: IntRange
            get() = if (isParallel) parallelCheck?.maxChain!! else sequentialCheck ?: IntRange.EMPTY
    }
}

fun List<CPF.Iteration>.finalResul() = map { it.program }.last().first()

fun List<CPF.Iteration>.resultOfGroupOperators(iteration: Int) =
        if (iteration != lastIndex)
            this[iteration + 1].program[this[iteration].groupedOperators.first]
        else null


class CPFChecker(var program: Program) {
    private inline val size get() = program.size
    private inline val indices get() = program.indices
    private inline val lastIndex get() = program.lastIndex

    private inline fun isWeekIndependency(i: Int, j: Int) = program.isWeekIndependency(i, j)
    private inline fun isWeekDependency(i: Int, j: Int) = program.isWeekDependency(i, j)

//    fun canBeCPF(): Boolean = CPFCheck().any(Result::canBeBringToCPF)

    fun CPFCheck() = pairsOfIndependent().map { CPFCheck(it.first, it.second) }

    private fun pairsOfIndependent(): List<Pair<Int, Int>> = indices
            .flatMap { i ->
                (i + 2..lastIndex).map { j -> i to j }
            }
            .filter { isWeekIndependency(it.first, it.second) }

    private fun CPFCheck(i: Int, j: Int): Result {
        assert(j > i + 1)
        assert(isWeekIndependency(i, j))

        val Ei = E(i)
        val Ej = E(j)
        val N1 = N1(i, j)
        val N2 = N2(i, j, N1)

        N2
                .filter { it in i + 1 until j }
                .forEach { k ->
                    if (N1 in E(k)) return Result(i, j, k, Ei, Ej, N1, N2, E(k))
                }

        val k = j - 1
        return Result(i, j, k, Ei, Ej, N1, N2, E(k))
    }

    private fun N1(i: Int, j: Int) = E(i) intersect E(j)
    private fun N2(i: Int, j: Int, N1: Set<Int> = N1(i, j)) = (E(i) union E(j)) - N1
    private fun E(i: Int) = weekDepsFrom(i)

    /**
     * @param i number of operator
     * @return {j | Si -> Sj}
     */
    private fun weekDepsFrom(i: Int) = (i + 1..lastIndex)
            .filter { j -> isWeekDependency(i, j) }
            .toCollection(LinkedHashSet())

    data class Result(
            val i: Int,
            val j: Int,
            val k: Int,
            val Ei: Set<Int>,
            val Ej: Set<Int>,
            val N1: Set<Int>,
            val N2: Set<Int>,
            val Ek: Set<Int>
    ) {
        val isSkInN2 = k in N2
        val isN1InEk = N1 in Ek
        val canBeBringToCPF = isSkInN2 && isN1InEk
    }
}

class ParallelGroupOperatorFormatter(var program: Program) {
    private inline val size get() = program.size
    private inline val indices get() = program.indices

    private inline fun isWeekIndependency(i: Int, j: Int) = program.isWeekIndependency(i, j)

    val C: Matrix
        get() {
            val matrix = List(size) { MutableList(size) { false } }

            for (i in 0 until size)
                for (j in i until size) {
                    matrix[i][j] = isWeekIndependency(i, j)
                    matrix[j][i] = matrix[i][j]
                }

            return matrix
        }

    private fun nextParallelGroupOperatorsRange(C: Matrix = this.C): IntRange? {
        fun squareSize(leftUpEdge: Int): Int {
            for (i in leftUpEdge until size) {
                val a = i - leftUpEdge
                for (j in 0..a)
                    if (!(C[leftUpEdge + j][i] && C[i][leftUpEdge + j])) return a
            }

            return size - leftUpEdge
        }

        for (i in 0 until size) {
            val squareSize = squareSize(i)
            if (squareSize < 2) continue

            val chain = i until i + squareSize
            val correctedChain = chain.correctChain()

            if (correctedChain.toList().size >= 2) {
                return correctedChain
            }
        }

        return null
    }

    private fun IntRange.correctChain(): IntRange {
        if (last == program.lastIndex) return this

        for (right in last downTo first) {
            if ((first..right).all { left -> program.isStrongDependency(left, right + 1) })
                return first..right
        }

        return IntRange.EMPTY
    }

    fun transform(result: Result = analysis) = result.toNewProgram(program)
    val analysis get() = result()
    private fun result(
            C: Matrix = this.C,
            U: Set<Int> = this.U,
            B1: Set<Int> = B1(C),
            K: Set<Int> = K(B1)
    ) = Result(C, U, B1, K)

    private val U get() = indices.toSet()

    private val B1 get() = B1(C)
    private fun B1(C: Matrix) = nextParallelGroupOperatorsRange(C)?.toSet() ?: emptySet()

    private val K get() = K(B1)
    private fun K(B1: Set<Int>) = U - B1


    data class Result(
            val C: Matrix,
            val U: Set<Int>,
            val B1: Set<Int>,
            val K: Set<Int>
    ) {
        inline val maxChain get() = if (B1.isNotEmpty()) B1.run { first()..last() } else null

        fun toNewProgram(from: Program): Program {
            val groupOperator = GroupOperator(from, maxChain!!, GroupOperator.Type.PARALLEL)
            val operators =
                    from.subList(0, maxChain!!.first) + groupOperator + from.subList(maxChain!!.last + 1, from.size)
            val newProgram = CashedProgram(operators)

            return newProgram
        }
    }
}

class SequentialGroupOperatorFormatter(var program: Program) {

    fun transform(maxChain: IntRange): Program {
        val groupOperator = GroupOperator(program, maxChain, GroupOperator.Type.SEQUENTIAL)
        val operators =
                program.subList(0, maxChain.first) + groupOperator + program.subList(maxChain.last + 1, program.size)
        val newProgram = CashedProgram(operators)

        return newProgram
    }

    val maxChain: IntRange?
        get() {
            val chains = formStrongDependencyChains()

            fun List<List<Int>>.correct(maxChain: IntRange): IntRange {
                val depsBeforeChain = this.take(maxChain.start).flatten()

                maxChain.forEach { i -> if (depsBeforeChain.contains(i)) return maxChain.start..(i - 1) }

                return maxChain
            }

            tailrec fun List<List<Int>>.chainEndFor(i: Int): Int =
                    if (!getOrNull(i).isNullOrEmpty() && get(i).first() == i + 1) chainEndFor(i + 1) else i

            fun List<List<Int>>.maxChain(i: Int) = correct(i..chainEndFor(i))


            for (i in program.indices) {
                val maxChain = chains.maxChain(i)
                if (maxChain.toList().size >= 2) return maxChain
            }

            return null
        }

    private fun formStrongDependencyChains() = program.indices.map { i ->
        (i + 1 until program.size).map { j ->
            program.isStrongDependency(i, j)
        }
                .mapIndexed { index, r -> if (r) index + i + 1 else null }
                .filterNotNull()
    }
}

