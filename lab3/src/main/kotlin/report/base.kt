package report


fun Int.toLatex() = (this + 1).toString()

fun Set<Int>.toLatex(inc: Int = 1) = map { it + inc }.map { it.toString() }.toSet().toLatex()

fun Set<String>.toLatex() =
    if (isNotEmpty())
        reduce { acc, v -> "$acc, $v" }.let { "\\{$it\\}" }
    else emptySetSymbol

const val emptySetSymbol = "\\varnothing"

fun Set<String>.toLatex(name: String) = toLatex().let { "$name = $it" }
