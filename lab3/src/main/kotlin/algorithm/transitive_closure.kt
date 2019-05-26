package transitiveclosure

import algorithm.ControlGraph
import algorithm.HasControlGraph
import algorithm.inputControlGraph

fun main() {
    val allVertex = 1..inputControlGraph.size

    println("====== TransitiveVertexType =====")
    allVertex.onEach { vertex ->
        println("==== $vertex")
        TransitiveVertexType(inputControlGraph, vertex).apply {
            (0 until S.size).forEach {
                println(it)
                println("S: ${S[it]}")
                println("T: ${T[it]}")
                println()
            }
        }
        println("==========")
    }
    println("==============================")


    println("====== InverseTransitiveVertexType =====")
    allVertex.onEach { vertex ->
        println("==== $vertex")
        InverseTransitiveVertexType(inputControlGraph, vertex).apply {
            (0 until S.size).forEach {
                println(it)
                println("S: ${S[it]}")
                println("T: ${T[it]}")
                println()
            }
        }
        println("==========")
    }
    println("==============================")
}

interface TransitiveVertex {
    val S: List<Set<Int>>
    val T: List<Set<Int>>
}

class TransitiveVertexType(controlGraph: ControlGraph, val startVertex: Int) : HasControlGraph(controlGraph),
    TransitiveVertex {
    override val S: List<Set<Int>>
    override val T: List<Set<Int>>

    val Tr get() = transitiveClosure
    val transitiveClosure get() = T.last()

    init {
        val S = mutableListOf(setOf<Int>(startVertex))
        val T = mutableListOf(setOf<Int>())

        do {
            T += (T.last() union S.last().G).toSortedSet()
            S += (T.last() subtract T.dropLast(1).last()).toSortedSet()
        } while (S.last().isNotEmpty())

        this.S = S
        this.T = T
    }
}

class InverseTransitiveVertexType(controlGraph: ControlGraph, val startVertex: Int) : HasControlGraph(controlGraph),
    TransitiveVertex {
    override val S: List<Set<Int>>
    override val T: List<Set<Int>>

    val invTr get() = inverseTransitiveClosure
    val inverseTransitiveClosure get() = T.last()

    init {
        val S = mutableListOf(setOf<Int>(startVertex))
        val T = mutableListOf(setOf<Int>())

        do {
            T += (T.last() union S.last().invG).toSortedSet()
            S += (T.last() subtract T.dropLast(1).last()).toSortedSet()
        } while (S.last().isNotEmpty())

        this.S = S
        this.T = T
    }
}