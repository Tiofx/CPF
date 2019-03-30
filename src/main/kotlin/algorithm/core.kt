package algorithm

open class Program(operators: List<IOperator>) : List<IOperator> by operators, Relations {
    inline fun S(i: Int) = get(i)

    override val operators: List<IOperator> get() = this

    fun I(operator: Int) = this[operator].R.filter { it !in this.take(operator).flatMap(IOperator::C) }.toSet()
    fun O(operator: Int) = this[operator].C.filter { it !in this.drop(operator + 1).flatMap(IOperator::C) }.toSet()
    val I get() = (0..lastIndex).flatMap { I(it) }.toCollection(LinkedHashSet(size))
    val O get() = (0..lastIndex).flatMap { O(it) }.toCollection(LinkedHashSet(size))
}

class GroupOperator(
        val from: Program,
        val range: IntRange,
        val type: Type
) : Relations {
    override val operators: List<IOperator> = from.subList(range.first, range.endInclusive + 1)

    enum class Type {
        PARALLEL,
        SEQUENTIAL
    }

    override fun toString() = "G\ncore.C = {$C}\ncore.R = {$R}"
}

class Operator(val expression: String) : IOperator {
    companion object {
        private val separators = listOf("+", "*", "-", "/", "(", ")", ",", ";", "[", "]").toTypedArray()
    }

    override val C = expression
            .split("=")
            .first()
            .trim()
            .let { linkedSetOf(it) }

    override val R = expression
            .split("=")[1]
            .split(*separators)
            .map(String::trim)
            .filter(String::isNotBlank)
            .filter { it.toFloatOrNull() == null }
            .toCollection(LinkedHashSet())

    inline operator fun component1() = C
    inline operator fun component2() = R

    override fun toString() = "C = {$C}\nR = {$R}"


}

typealias Matrix = List<List<Boolean>>

