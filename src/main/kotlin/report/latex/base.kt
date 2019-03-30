package report.latex

import algorithm.Matrix
import report.*

fun OperatorName.toLatex() = when (this) {
    is OriginOperator -> "S_{$i}"
    is GroupParallelOperator -> "\\bar{\\bar{y}}_{$i}"
    is GroupSequentialOperator -> "\\bar{y}_{$i}"
    UnknownName -> "Unknown"
}

fun Matrix.toLatexAsRelations(operatorNames: List<String>, connector: String) =
        mapIndexed { i, row -> row.mapIndexed { j, el -> Triple(i, j, el) } }
                .flatten()
                .filter(Triple<Int, Int, Boolean>::third)
                .map { operatorNames[it.first] to operatorNames[it.second] }
                .joinToString(LatexConverter.singleLineBreak) { "${it.first} $connector ${it.second}" }
                .let {
                    """
\begin{math}
    $it
\end{math}
                    """.trimIndent()
                }


fun Matrix.toLatex(
        rowHeader: List<String>,
        colHeader: List<String>,
        highlight: Pair<IntRange, IntRange> = IntRange.EMPTY to IntRange.EMPTY
) =
        toRaw(highlight)
                .wrapBy(rowHeader.highlight(highlight.second), colHeader.highlight(highlight.first))
                .toLatex(this.size)


private fun Matrix.toRaw(highlight: Pair<IntRange, IntRange> = IntRange.EMPTY to IntRange.EMPTY) =
        mapIndexed { row, it ->
            it.map { b -> if (b) "1" else "0" }
                    .mapIndexed { col, el ->
                        if (row in highlight.first && col in highlight.second) el.highlight() else el
                    }
        }.map { it.reduce { acc, v -> "$acc & $v" } }

private fun List<String>.highlight(highlight: IntRange) =
        mapIndexed { index, el -> if (index in highlight) el.highlight("\\normalsize") else el }

private fun List<String>.wrapBy(rowHeader: List<String>, colHeader: List<String>) =
        mapIndexed { i, row -> "${rowHeader[i]} & $row" }
                .reduce { acc, row -> "$acc \\cr\n$row" }
                .let {
                    listOf(colHeader.fold("  ") { acc, h -> "$acc & $h" }) + it
                }

private fun List<String>.toLatex(size: Int) =
        joinToString("\\cr\n").let {
            """
                |{${size.sizeModifier()}{$\bbordermatrix{
                |$it
                |}$}}
            """.trimMargin()
        }


private fun String.highlight(size: String = "\\LARGE") = "$$size \\boldmath$$this$$ "


fun Int.sizeModifier() = when {
    this >= 30 -> "\\resizebox{\\linewidth}{!}"
//    this >= 30 -> "\\let\\quad\\thinspace\\tiny"
    this >= 25 -> "\\let\\quad\\thinspace\\scriptize"
    this >= 15 -> "\\let\\quad\\thinspace\\footnotesize"
    else -> "\\let\\quad\\thinspace\\normalsize"
}

fun Set<String>.toLatex() =
        if (isNotEmpty())
            reduce { acc, v -> "$acc, $v" }.let { "\\{$it\\}" }
        else emptySetSymbol

private const val emptySetSymbol = "\\varnothing"

fun Set<String>.toLatex(name: String) = toLatex().let { "$name = $it" }