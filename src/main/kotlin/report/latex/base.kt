package report.latex

import algorithm.Matrix
import report.*

fun OperatorName.toLatex() = when (this) {
    is OriginOperator -> "S_{$i}"
    is GroupParallelOperator -> "\\bar{\\bar{y}}_{$i}"
    is GroupSequentialOperator -> "\\bar{y}_{$i}"
    UnknownName -> "Unknown"
}

fun Matrix.toLatex(rowHeader: List<String>, colHeader: List<String>) =
        map { it.map { b -> if (b) "1" else "0" } }
                .map { it.reduce { acc, v -> "$acc & $v" } }
                .mapIndexed { i, row -> "${rowHeader[i]} & $row" }
                .reduce { acc, row -> "$acc \\cr\n$row" }
                .let {
                    """
            |${colHeader.fold("  ") { acc, h -> "$acc & $h" }} \cr
            |$it
        """.trimMargin()
                }
                .let {
                    """
            |{${sizeModifier()}{$\bbordermatrix{
            |$it
            |}$}}
        """.trimMargin()
                }

fun Matrix.sizeModifier() = when {
    size >= 30 -> "\\resizebox{\\linewidth}{!}"
//    size >= 30 -> "\\let\\quad\\thinspace\\tiny"
    size >= 25 -> "\\let\\quad\\thinspace\\scriptize"
    size >= 15 -> "\\let\\quad\\thinspace\\footnotesize"
    else -> "\\let\\quad\\thinspace\\normalsize"
}

fun Set<String>.toLatex() =
        if (isNotEmpty())
            reduce { acc, v -> "$acc, $v" }.let { "\\{$it\\}" }
        else emptySetSymbol

private const val emptySetSymbol = "\\varnothing"

fun Set<String>.toLatex(name: String) = toLatex().let { "$name = $it" }