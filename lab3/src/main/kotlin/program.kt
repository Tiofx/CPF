package program

import kotlinx.coroutines.*
import program.Log.logIn
import program.Log.logOut
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


fun main() {
    val (A, B, C) = Triple(2F, 3F, 4F)

    val sequentialResult = SequentialProgram(A, B, C).execute()
    val sequentialLog = Log.content

    val parallelProgramByType = ParallelProgramByVertexType(A, B, C).execute()
    val parallelLog = Log.content

    val parallelProgramByPrototype = ParallelProgramByVertexPrototype(A, B, C).execute()
    val parallelByPrototypeLog = Log.content

    require(abs(sequentialResult - parallelProgramByType) < 1e-4)
    require(abs(parallelProgramByPrototype - parallelProgramByType) < 1e-4)

    println("Последовательное программа:")
    println(sequentialLog)
    println()
    println("Параллельное программа (образ):")
    println(parallelLog)
    println()
    println("Параллельное программа (прообраз):")
    println(parallelByPrototypeLog)
    println()

    println("Входные данные:")
    println("A = $A, B = $B, C = $C")
    println("Результат:")
    println("F = $sequentialResult")


}

class ParallelProgramByVertexPrototype(A: Float, B: Float, C: Float) : ParallelProgram(A, B, C) {
    override val executeOrder: Operator by lazy {
        a1 before
                (a2 and a3) before
                (a4 and a5) before
                a6 before
                a7 before
                (a8 and a9) before
                (a10 and a11) before
                a12 before
                a13
    }
}

class ParallelProgramByVertexType(A: Float, B: Float, C: Float) : ParallelProgram(A, B, C) {
    override val executeOrder: Operator by lazy {
        a1 before
                a3 before
                a5 before
                (a2 and a4 and a6) before
                a7 before
                a9 before
                a11 before
                (a8 and a10 and a12) before
                a13
    }
}

abstract class ParallelProgram(A: Float, B: Float, C: Float) : Program(A, B, C) {

    protected inline val a1 get() = operators[0]
    protected inline val a2 get() = operators[1]
    protected inline val a3 get() = operators[2]
    protected inline val a4 get() = operators[3]
    protected inline val a5 get() = operators[4]
    protected inline val a6 get() = operators[5]
    protected inline val a7 get() = operators[6]
    protected inline val a8 get() = operators[7]
    protected inline val a9 get() = operators[8]
    protected inline val a10 get() = operators[9]
    protected inline val a11 get() = operators[10]
    protected inline val a12 get() = operators[11]
    protected inline val a13 get() = operators[12]
}


class SequentialProgram(A: Float, B: Float, C: Float) : Program(A, B, C) {
    override val executeOrder: Operator by lazy { operators.reduce { acc, o -> acc before o } }
}

abstract class Program(val A: Float, val B: Float, val C: Float) {
    private var D: Float = 0F
    private var E: Float = 0F
    private val conditions = BooleanArray(6) { false }
    private var F: Float = 0F

    protected val operators: List<Operator> by lazy {
        listOf<suspend () -> Unit>(
            { conditions[0] = A > 0 },
            { if (conditions[0]) D = A * A + cos(A) },
            { conditions[1] = A < 0 },
            { if (conditions[1]) D = sin(A * A + 10) },
            { conditions[2] = A == 0F },
            { if (conditions[2]) D = 10F },
            { conditions[3] = B > C },
            { if (conditions[3]) E = 2 * B / C },
            { conditions[4] = B < C },
            { if (conditions[4]) E = sqrt(abs(B + 2 * C)) },
            { conditions[5] = B == C },
            { if (conditions[5]) E = 0F },
            { F = abs(2 * B - D) * E - (A * A + E * E) / C }
        ).map { SimpleOperator(it) }
    }

    protected abstract val executeOrder: Operator

    fun execute(withDelay: Boolean = false): Float {
        Log.reset()
        preExecute(withDelay)
        runBlocking { executeOrder.execute(withDelay) }

        return F
    }

    protected open fun preExecute(withDelay: Boolean) {
        Log.reset()
        executeOrder.setDepth()
    }
}


sealed class Operator

class SequentialGroupOperator(val operators: List<Operator>) : Operator()
class ParallelGroupOperator(val operators: List<Operator>) : Operator()

class SimpleOperator(
    val action: suspend () -> Unit,
    val operatorName: String = Log.run { "a$simpleOperatorNumber".also { simpleOperatorNumber++ } }
) : Operator() {
    var depth: Int = -1
}


suspend fun Operator.execute(withDelay: Boolean = false) {
    when (this) {
        is SimpleOperator -> {
            logIn()
            if (withDelay) {
                delay(200)
            }
            action()
            logOut()
        }

        is SequentialGroupOperator -> {
            operators.forEach {
                GlobalScope.async { it.execute(withDelay) }.await()
                if (withDelay) {
                    delay(200)
                }
            }
        }

        is ParallelGroupOperator -> {
            operators.map { GlobalScope.async { it.execute(withDelay) } }.awaitAll()
            if (withDelay) {
                delay(200)
            }
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


object Log {
    private val log = mutableListOf<String>()
    val content get() = log.joinToString("\n")

    var simpleOperatorNumber = 1


    fun SimpleOperator.logIn() {
        synchronized(log) {
            log += " ${"-".repeat(2 * depth)}> $operatorName"
        }
    }

    fun SimpleOperator.logOut() {
        synchronized(log) {
            log += "<${"-".repeat(2 * depth)}  $operatorName"
        }
    }

    fun reset() {
        simpleOperatorNumber = 1
        log.clear()
    }
}