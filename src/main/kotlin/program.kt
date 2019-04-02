sealed class Operator

class SequentialGroupOperator(val operators: List<Operator>) : Operator()
class ParallelGroupOperator(val operators: List<Operator>) : Operator()
class SimpleOperator(val action: suspend () -> Unit) : Operator() {
    val operatorName = "S$counter".also { counter++ }
    var depth: Int = -1

    companion object {

        private var startTime: LocalTime? = null
        private var counter = 1
        private val passedTime get() = Duration.between(startTime, LocalTime.now()).toMillis()
        private val log = mutableListOf<String>()

        fun reset() {
            startTime = null
            counter = 1
            counter = 1
            log.clear()
        }

        fun start() {
            startTime = LocalTime.now()
        }

        fun printLog() {
            println(log.joinToString("\n"))
        }
    }

    fun logIn() {
        log += String.format("[%4d] ", passedTime) + " ${"-".repeat(2 * depth)}> $operatorName"
    }

    fun logOut() {
        log += String.format("[%4d] ", passedTime) + "<${"-".repeat(2 * depth)}  $operatorName"
    }

}


suspend fun Operator.execute() {
    when (this) {
        is SimpleOperator -> {
            logIn()
            action()
            logOut()
        }

        is SequentialGroupOperator -> {
            operators.forEach {
                GlobalScope.async { it.execute() }.await()
            }
        }

        is ParallelGroupOperator -> {
            operators.map { GlobalScope.async { it.execute() } }.awaitAll()
        }
    }
}

fun Operator.setDepth(depth: Int = 0) {
    when (this) {
        is SimpleOperator -> this.depth = depth
        is ParallelGroupOperator -> operators.forEach { it.setDepth(depth + 1) }
        is SequentialGroupOperator -> operators.forEach { it.setDepth(depth + 1) }
    }
}

infix fun Operator.before(second: Operator) =
        when {
            this is SequentialGroupOperator && second is SequentialGroupOperator -> SequentialGroupOperator(operators + second.operators)
            this is SequentialGroupOperator -> SequentialGroupOperator(operators + second)
            second is SequentialGroupOperator -> SequentialGroupOperator(listOf(this) + second.operators)

            else -> SequentialGroupOperator(listOf(this, second))
        }

infix fun Operator.and(second: Operator) =
        when {
            this is ParallelGroupOperator && second is ParallelGroupOperator -> ParallelGroupOperator(operators + second.operators)
            this is ParallelGroupOperator -> ParallelGroupOperator(operators + second)
            second is ParallelGroupOperator -> ParallelGroupOperator(listOf(this) + second.operators)

            else -> ParallelGroupOperator(listOf(this, second))
        }
