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

//val rawProgram_v1 = listOf(
//    "a2233 = a22*a33",
//    "a2332 = a23*a32",
//    "A11 = a2233 - a2332",
//
//    "a2133 = a21*a33",
//    "a2331 = a23*a31",
//    "A12 = -(a2133 - a2331)",
//
//    "a2132 = a21*a32",
//    "a2232 = a22*a32",
//    "A13 = a2132 - a2232",
//
//    "aA1111 = a11*A11",
//    "aA1212 = a12*A12",
//    "aA1313 = a13*A13",
//    "det = aA1111 + aA1212 + aA1313",
//
//
//    "a1233  = a12*a33",
//    "a1332 = a13*a32",
//    "A21 = -(a1233 - a1332)",
//
//    "a1133 = a11*a33",
//    "a1331 = a13*a31",
//    "A22 = a1133 - a1331",
//
//    "a1133 = a11*a33",
//    "a1231 = a12*a31",
//    "A23 = -(a1133 - a1231)",
//
//
//    "a1223 = a12*a23",
//    "a1322 = a13*a22",
//    "A31 = a1223 - a1322",
//
//    "a1123 = a11*a23",
//    "a1321 = a13*a21",
//    "A32 = -(a1123 - a1321)",
//
//    "a1122 = a11*a22",
//    "a1221 = a12*a21",
//    "A33 = a1122 - a1221",
//
//
//    "a^_1_11 = A11 / det",
//    "a^_1_12 = A21 / det",
//    "a^_1_13 = A31 / det",
//
//    "a^_1_21 = A12 / det",
//    "a^_1_22 = A22 / det",
//    "a^_1_23 = A12 / det",
//
//    "a^_1_31 = A13 / det",
//    "a^_1_32 = A23 / det",
//    "a^_1_33 = A33 / det"
//)

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
