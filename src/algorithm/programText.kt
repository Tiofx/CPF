package algorithm

import java.nio.file.Paths

typealias ProgramText = List<String>

fun ProgramText.prepareToLatex(): ProgramText {
    data class ModifierItem(
        val from: String,
        val by: (MatchResult) -> CharSequence
    )

    fun modifire(from: String, by: (String) -> CharSequence) =
        ModifierItem(from) { by(it.destructured.component1()) }

    val items = listOf(
        ModifierItem("det") { "\\Delta" },
        modifire("a\\^_1_(\\d{2})") { "a^{-1}_{$it}" },
        modifire("aA(\\d{4})") { "aA_{$it}" },
        modifire("A(\\d{2})") { "A_{$it}" },
        modifire("a(\\d{4})") { "a_{$it}" },
        modifire("a(\\d{2})") { "a_{$it}" }
    )


    var text = joinToString("\n")

    items.forEach {
        text = it.from.toRegex().replace(text, it.by)
    }

    return text.split("\n")
}


val programText
    get() = Paths
        .get("")
        .resolve("raw_program.txt")
        .toFile()
        .readLines()
        .filter { it.isNotBlank() }


val demo1 = listOf(
    "A11 = a22*a33 - a23*a32",
    "A12 = -(a21*a33 - a23*a31)",
    "aA1111 = a11*A11",
    "aA1212 = a12*A12",
    "det = aA1111 + aA1212",

    "A21 = a22*a33 - a23*a32",
    "a^_1_11 = A11 / det",
    "a^_1_21 = A12 / det",
    "a^_1_12 = A21 / det"
)
