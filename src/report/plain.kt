package report

import algorithm.CPF
import algorithm.Matrix
import algorithm.Program
import report.latex.toLatex

fun List<CPF.Iteration>.description(namer: OperatorNamer) {
    forEach {
        it.description(it.program, namer)
    }
}

fun CPF.Iteration.description(program: Program, namer: OperatorNamer) {
    println("================== $number ===========================")
    println(namer.names(program).map(OperatorName::toPlainString))
//            fpfCheck.map { it.canBeBringToCPF to it }.forEach(::println)
    println()

    if (parallelCheck != null) {
        println(parallelCheck.C.toLatex("S\n".repeat(40).split("\n"), "S\n".repeat(40).split("\n")))
        println(parallelCheck.C.toMatrixString())
        println(parallelCheck.copy(C = emptyList()))
        println(parallelCheck.maxChain)
    }

    if (sequentialCheck != null) {
        println(sequentialCheck)
    }

    println()
}

private fun List<CPF.Iteration>.shortDesription() {
    println("Description:")
    println("Iteration number: $size")
    forEachIndexed { i, it ->
        println(
            """
            Iteration number:   ${i + 1}
            core.Operator number:    ${it.program.size}
            core.CPF check number:   ${it.fpfCheck.size}

        """.trimIndent()
        )
    }
    println("Total number of cpf check ${map { it.fpfCheck.size }.sum()}")
}


fun OperatorName.toPlainString() = when (this) {
    is OriginOperator -> "S$i"
    is GroupParallelOperator -> "||y$i"
    is GroupSequentialOperator -> "|y$i"
    UnknownName -> "Unknown"
}

fun Matrix.toMatrixString() =
    map { it.map { b -> if (b) 1 else 0 } }
        .map { it.toString() + "\n" }
        .reduce { acc, s -> acc + s }
        .replace("[\\[\\]]".toRegex(), "|")
        .replace(",", "")
