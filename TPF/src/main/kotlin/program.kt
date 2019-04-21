package program

import kotlinx.coroutines.*
import program.Log.logIn
import program.Log.logOut
import kotlin.math.abs


fun main() {
    val a = floatArrayOf(0.9F, 3F, 1.1F, 4F, 6F, 17F)
    val x = 1.4F
    val n = 5

    val sequentialResult = SequentialProgram(a, n, x).execute()
    val sequentialLog = Log.content

    val parallelProgram = ParallelInvMatrixProgram(a, n, x)
    val parallelResult = parallelProgram.execute()
    println(parallelResult)
    val parallelLog = Log.content

    val parallelResultWithDelay = ParallelInvMatrixProgram(a, n, x).execute(true)
    val parallelWithDelayLog = Log.content

    require(abs(sequentialResult - parallelResult) < 1e-4)
    require(abs(parallelResultWithDelay - parallelResult) < 1e-4)

    println("Последовательное выполнение:")
    println(sequentialLog)
    println()
    println("Параллельное выполнение:")
    println(parallelLog)
    println()
    println("Параллельное выполнение (c задержкой):")
    println(parallelWithDelayLog)
    println()

    println("Входные данные:")
    println(
        """
a = ${a.toList()}
x = $x
n = $n
    """.trimIndent()
    )
    println("Реузльтат:")
    println("Y = $sequentialResult")

    println("Состояния памяти:")
    parallelProgram.memoryStates.forEachIndexed { i, state ->
        fun FloatArray.toReadableFormat() = joinToString(", ", "[", "]") { String.format("% 6.2f", it) }
        println(i)
        state.run {
            println(
                """
a   = ${this.a.toReadableFormat()}
x   = ${this.x}
n   = ${this.n}
t   = ${t.toReadableFormat()}
n2  = ${n2.toReadableFormat()}
phi = ${phi.toReadableFormat()}
temp= ${temp.toReadableFormat()}
Y   = $Y

            """.trimIndent()
            )
        }
    }

}

class ParallelInvMatrixProgram(a: FloatArray, n: Int, x: Float) : PolynomialCompumaticProgram(a, n, x) {

    val memoryStates = mutableListOf<MemoryState>()
    val dumpMemoryOperator by lazy { SimpleOperator({ memoryStates += dump() }, "memory dump") }

    override val executeOrder: Operator by lazy {
        dumpMemoryOperator before (
                (a1 and a2 and a3 and a4 and a5 and a6 and a7 and a8) before dumpMemoryOperator before
                        (a9 and a10 and a13 and a17 and a21 and a25) before dumpMemoryOperator before
                        (a11 and a14 and a28) before dumpMemoryOperator before
                        (a12 and a18 and a29) before dumpMemoryOperator before
                        a15 before dumpMemoryOperator before
                        (a16 and a22 and a30) before dumpMemoryOperator before
                        a19 before dumpMemoryOperator before
                        (a20 and a26 and a31) before dumpMemoryOperator before
                        a23 before dumpMemoryOperator before
                        (a24 and a32) before dumpMemoryOperator before
                        a27 before dumpMemoryOperator before
                        a33 before dumpMemoryOperator before
                        a34 before dumpMemoryOperator)
    }

    private inline val a1 get() = operators[0]
    private inline val a2 get() = operators[1]
    private inline val a3 get() = operators[2]
    private inline val a4 get() = operators[3]
    private inline val a5 get() = operators[4]
    private inline val a6 get() = operators[5]
    private inline val a7 get() = operators[6]
    private inline val a8 get() = operators[7]
    private inline val a9 get() = operators[8]
    private inline val a10 get() = operators[9]
    private inline val a11 get() = operators[10]
    private inline val a12 get() = operators[11]
    private inline val a13 get() = operators[12]
    private inline val a14 get() = operators[13]
    private inline val a15 get() = operators[14]
    private inline val a16 get() = operators[15]
    private inline val a17 get() = operators[16]
    private inline val a18 get() = operators[17]
    private inline val a19 get() = operators[18]
    private inline val a20 get() = operators[19]
    private inline val a21 get() = operators[20]
    private inline val a22 get() = operators[21]
    private inline val a23 get() = operators[22]
    private inline val a24 get() = operators[23]
    private inline val a25 get() = operators[24]
    private inline val a26 get() = operators[25]
    private inline val a27 get() = operators[26]
    private inline val a28 get() = operators[27]
    private inline val a29 get() = operators[28]
    private inline val a30 get() = operators[29]
    private inline val a31 get() = operators[30]
    private inline val a32 get() = operators[31]
    private inline val a33 get() = operators[32]
    private inline val a34 get() = operators[33]

    override fun preExecute(withDelay: Boolean) {
        super.preExecute(withDelay)
        memoryStates.clear()
    }
}

class SequentialProgram(a: FloatArray, n: Int, x: Float) : PolynomialCompumaticProgram(a, n, x) {
    override val executeOrder: Operator by lazy { operators.reduce { acc, o -> acc before o } }
}

abstract class PolynomialCompumaticProgram(
    a: FloatArray,
    n: Int,
    x: Float
) {
    val a = FloatArray(6) { 0F }
    private var x: Float = 0F
    private var n: Int = 0
    private var Y: Float = 0F

    private val phi = FloatArray(6) { 0F }

    private val n2 = FloatArray(4) { 0F }
    private val t = FloatArray(9) { 0F }
    private val temp = FloatArray(6) { 0F }


    data class MemoryState(
        val a: FloatArray,
        val x: Float,
        val n: Int,
        val t: FloatArray,
        val n2: FloatArray,
        val phi: FloatArray,
        val temp: FloatArray,
        val Y: Float
    )

    fun dump() = MemoryState(this.a.clone(), this.x, this.n, t.clone(), n2.clone(), phi.clone(), temp.clone(), Y)

    protected val operators: List<Operator> by lazy {
        listOf<suspend () -> Unit>(
            { this.a[0] = a[0] },
            { this.a[1] = a[1] },
            { this.a[2] = a[2] },
            { this.a[3] = a[3] },
            { this.a[4] = a[4] },
            { this.a[5] = a[5] },
            { this.n = n },
            { this.x = x },

            { phi[0] = 1F },
            { t[0] = (n + 1) / 2F },
            { phi[1] = x - t[0] },
            { t[1] = phi[1] * phi[1] },
            { n2[0] = n * n - 1F },
            { t[2] = phi[0] * n2[0] / 12F },
            { phi[2] = t[1] - t[2] },
            { t[3] = phi[1] * phi[2] },
            { n2[1] = n * n - 4F },
            { t[4] = phi[1] * n2[1] / 15F },
            { phi[3] = t[3] - t[4] },
            { t[5] = phi[1] * phi[3] },
            { n2[2] = n * n - 9F },
            { t[6] = 9 * phi[2] * n2[2] / 140F },
            { phi[4] = t[6] - t[5] },
            { t[7] = phi[1] * phi[4] },
            { n2[3] = n * n - 16F },
            { t[8] = 4 * phi[3] * n2[3] / 63F },
            { phi[5] = t[8] - t[7] },
            { temp[0] = a[0] * phi[0] },
            { temp[1] = a[1] * phi[1] },
            { temp[2] = a[2] * phi[2] },
            { temp[3] = a[3] * phi[3] },
            { temp[4] = a[4] * phi[4] },
            { temp[5] = a[5] * phi[5] },
            { Y = temp[0] + temp[1] + temp[2] + temp[3] + temp[4] + temp[5] }
        ).map { SimpleOperator(it) }
    }

    protected abstract val executeOrder: Operator

    fun execute(withDelay: Boolean = false): Float {
        preExecute(withDelay)
        runBlocking { executeOrder.execute(withDelay) }

        return Y
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