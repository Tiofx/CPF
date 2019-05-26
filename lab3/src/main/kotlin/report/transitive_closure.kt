package report

import algorithm.inputControlGraph
import transitiveclosure.InverseTransitiveVertexType
import transitiveclosure.TransitiveVertexType

fun main() {
    val allVertex = 1..inputControlGraph.size

    println("====== TransitiveVertexType =====")
    allVertex.onEach { vertex ->
        println("==== $vertex")
        TransitiveVertexType(inputControlGraph, vertex).apply {
            println(toLatex())
        }
        println("==========")
    }
    println("==============================")


    println("====== InverseTransitiveVertexType =====")
    allVertex.onEach { vertex ->
        println("==== $vertex")
        InverseTransitiveVertexType(inputControlGraph, vertex).apply {
            println(toLatex())
        }
        println("==========")
    }
    println("==============================")
}

fun TransitiveVertexType.toLatex() =
    TransitiveVertex(this).toLatex("Tr(a_{$startVertex})")

fun InverseTransitiveVertexType.toLatex() =
    TransitiveVertex(this).toLatex("Tr^{-1}(a_{$startVertex})")


data class TransitiveVertex(
    val closure: transitiveclosure.TransitiveVertex
) {
    val S = closure.S.mapIndexed { i, set ->
        set.map(Int::toOperation).toSet().toLatex("S^{($i)} &")
    }

    val T = closure.T.mapIndexed { i, set ->
        set.map(Int::toOperation).toSet().toLatex("T^{($i)} &")
    }

    fun toLatex(resultName: String) = S.zip(T)
        .joinToString("\\\\\n") { (s, t) -> "$s\\\\\n$t" }
        .let {
            """
\begin{align*}
$it \\
$resultName &= ${T.last().drop(T.last().indexOf('=') + 1)}
\end{align*}
            """.trimIndent()
        }
}


val Int.toOperation get() = "a_{$this}"