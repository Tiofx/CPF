package algorithm

interface IOperator {
    val C: Set<String>
    val R: Set<String>
}

val IOperator.V get() = C union R


interface IGroupOperator : IOperator {
    val operators: List<IOperator>

    override val C get() = operators.flatMap(IOperator::C).toSet()
    override val R get() = operators.flatMap(IOperator::R).toSet()
}

inline val IGroupOperator.indices get() = operators.indices
inline val IGroupOperator.lastIndex get() = operators.lastIndex
inline val IGroupOperator.size get() = operators.size

fun IGroupOperator.C(i: Int) = operators[i].C
fun IGroupOperator.R(i: Int) = operators[i].R
fun IGroupOperator.V(i: Int) = operators[i].V

fun IGroupOperator.C(p: Pair<Int, Int>) = operators.subList(p.first, p.second + 1).flatMap(IOperator::C).toSet()
fun IGroupOperator.R(p: Pair<Int, Int>) = operators.subList(p.first, p.second + 1).flatMap(IOperator::R).toSet()


interface Relations : IGroupOperator {

    fun isStrongDependency(i: Int, j: Int): Boolean {
        assert(i in indices && j in indices)
        assert(i < j)

        val base = C(i) intersect R(j)

        return if (i + 1 == j)
            base.isNotEmpty()
        else
            base !in C(i + 1 to j - 1)
    }

    fun isWeekDependency(i: Int, j: Int): Boolean {
        assert(i in indices && j in indices)
        assert(i < j)

        if (isStrongDependency(i, j)) return true

        for (k in i + 1 until j) {
            if (isStrongDependency(i, k) && isWeekDependency(k, j)) return true
        }

        return false
    }

    fun isWeekIndependency(i: Int, j: Int): Boolean {
        assert(i in indices && j in indices)
//        assert(i <= j)
        if (i == j) return true

        val noInfoDep = (C(i) intersect R(j)).isEmpty()
        val noCompetitiveDep = (R(i) intersect C(j)).isEmpty()

        return noInfoDep && noCompetitiveDep && !isWeekDependency(i, j)
    }

    fun isStrongIndependency(i: Int, j: Int): Boolean {
        assert(i in indices && j in indices)
        assert(i <= j)
        if (i == j) return true

        return (C(i) intersect V(j)).isEmpty() && (V(i) intersect C(j)).isEmpty() && !isWeekDependency(i, j)
    }
}


