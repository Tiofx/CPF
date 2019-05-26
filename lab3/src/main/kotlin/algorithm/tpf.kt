package tpf

import algorithm.ControlGraph
import algorithm.HasControlGraph
import algorithm.contains
import algorithm.inputControlGraph

fun main() {
    val byType = TPFByVertexType(inputControlGraph)
    val byPrototype = TPFByVertexPrototype(inputControlGraph)

    println("====== TPFByVertexType =====")
    byType.S.forEachIndexed { index, set ->
        println(index)
        println(set)
        println()
    }
    println("==============================")


    println("====== TPFByVertexPrototype =====")
    byPrototype.S.forEachIndexed { index, set ->
        println(index)
        println(set)
        println()
    }
    println("==============================")
}


class TPFByVertexType(controlGraph: ControlGraph) : HasControlGraph(controlGraph) {
    val S: List<Set<Int>>

    init {
        val S = mutableListOf(emptySet<Int>())

        while (S.flatten().toSet().size != controlGraph.size) {
            val Sn = controlGraph.run {
                (1..size)
                    .filter { it !in S.flatten() }
                    .filter { G(it) in S.flatten().toSet() }
            }.toSet()

            S += Sn
        }

        this.S = S.drop(1).reversed()
    }

}

class TPFByVertexPrototype(controlGraph: ControlGraph) : HasControlGraph(controlGraph) {
    val S: List<Set<Int>>

    init {
        val S = mutableListOf(emptySet<Int>())

        while (S.flatten().toSet().size != controlGraph.size) {
            val Sn = controlGraph.run {
                (1..size)
                    .filter { it !in S.flatten() }
                    .filter { invG(it) in S.flatten().toSet() }
            }.toSet()

            S += Sn
        }

        this.S = S.drop(1)
    }

}