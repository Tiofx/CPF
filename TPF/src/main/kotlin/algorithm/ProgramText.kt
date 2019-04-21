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
        modifire("phi(\\d+)") { "\\phi_{$it}" },
        modifire("n2(\\d+)") { "n^2_{$it}" },
        modifire("t(\\d+)") { "t_{$it}" },
        modifire("temp(\\d+)") { "temp_{$it}" },
        modifire("a(\\d+)") { "a_{$it}" }
    )


    var text = joinToString("\n")

    items.forEach {
        text = it.from.toRegex().replace(text, it.by)
    }

    return text.split("\n")
}

val RESOURCES_FOLDER = Paths
    .get("")
    .resolve("src")
    .resolve("main")
    .resolve("resources")

val programText
    get() = RESOURCES_FOLDER
        .resolve("raw_program.txt")
        .toFile()
        .readLines()
        .filter { it.isNotBlank() }
        .prepareToLatex()


val demo1 = listOf(
    "a22 = 1",
    "a33 = 2",
    "a23 = 2",
    "a32 = 2",
    "a21 = 2",
    "a11 = 2",
    "a12 = 2",
    "a31 = 2",
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
