package algorithm

class Program(val operators: List<Operation>) {
    companion object {
        fun demo() = from(demo1)

        fun from(rawProgram: List<String> = programText): Program {
            class RawOperation(val j: Int, expression: String) : OperationParser(expression)

            val rawOperations = rawProgram.mapIndexed { i, rawOp -> RawOperation(i, rawOp) }

            val W = rawOperations.mapIndexed { i, o ->
                rawOperations
                    .drop(i)
                    .mapIndexed { j, secondO -> if (o.ouput.any { it in secondO.input }) j + i else null }
                    .filterNotNull()
            }

            return (0 until rawProgram.size)
                .map {
                    rawOperations[it].run {
                        Operation(j, input, ouput, W[it].toSet())
                    }
                }
                .let { Program(it) }
        }
    }

    val input get() = operators.run {
        map(Operation::input) subtract (map(Operation::input) intersect  map(Operation::output))
    }
}

interface IOperationParser {
    val input: Set<String>
    val ouput: Set<String>
}

open class OperationParser(val expression: String) : IOperationParser {
    companion object {
        private val separators = listOf("+", "*", "-", "/", "(", ")", ",", ";", "[", "]").toTypedArray()
    }

    override val ouput = expression
        .split("=")
        .first()
        .trim()
        .let { linkedSetOf(it) }

    override val input = expression
        .split("=")[1]
        .split(*separators)
        .map(String::trim)
        .filter(String::isNotBlank)
        .filter { it.toFloatOrNull() == null }
        .toCollection(LinkedHashSet())
}

data class Operation(
    val j: Int,
    val input: Set<String>,
    val output: Set<String>,
    val w: Set<Int>
) {
    val type: Arity = Arity(input.size)
    val inNumber get() = input.size
    val wNumber get() = w.size

    class Arity(val n: Int) {
        override fun toString(): String {
            return when (n) {
                1 -> "унарная"
                2 -> "бинарная"
                3 -> "тернарная"
                else -> "$n-арная"
            }
        }
    }
}