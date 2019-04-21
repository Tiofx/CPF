package report

import algorithm.Operation

fun Int.toLatex() = (this + 1).toString()

fun Set<Int>.toLatex(inc: Int = 1) = map { it + inc }.map { it.toString() }.toSet().toLatex()

fun Set<String>.toLatex() =
    if (isNotEmpty())
        reduce { acc, v -> "$acc, $v" }.let { "\\{$it\\}" }
    else emptySetSymbol

const val emptySetSymbol = "\\varnothing"

fun Set<String>.toLatex(name: String) = toLatex().let { "$name = $it" }

fun Operation.toLatex(operationText: String): LatexOperator =
    LatexOperator(
        (j + 1).toString(),
        operationText,
        type.toString(),
        input.toLatex(),
        output.toLatex(),
        w.map { it + 1 }.map { it.toString() }.toSet().toLatex(),
        inNumber.toString(),
        wNumber.toString()
    )

data class LatexOperator(
    val j: String,
    val operationText: String,
    val type: String,
    val input: String,
    val output: String,
    val w: String,
    val inNumber: String,
    val wNumber: String
) {
    fun toList() = listOf(j, operationText, type, input, output, w, inNumber, wNumber)
}