package report

import algorithm.TPF


fun List<TPF.ArityCondition>.toLatex(asShortForm: Boolean = true) =
    map { it.toLatex() }
        .map {
            "Для арности ${it.arity}:\n ${if (asShortForm) it.toShortLatex() else it.fullCase.joinToString("\\\\\n") { it.toLatex() }}"
        }
        .joinToString("\n\n")

data class ArityCondition(
    val arity: Int,
    val tierK: Int,
    val fullCase: List<SingleCase>,
    val result: String
) {

    fun toLatex() = fullCase.joinToString("\\\\ \n") { it.toLatex() }
        .let {
            """
            |\begin{cases}
            |$it
            |\end{cases}
        """.trimMargin()
        }

    fun toShortLatex() = fullCase.joinToString("\\\\ \n") {
        it.stepSequence.run { first() + " = " + last() }
    }.let {
        """
            |\begin{cases}
            |$it
            |\end{cases}
        """.trimMargin()
    }
}

data class SingleCase(
    val tierK: Int,
    val parts: List<SingleCasePart>,
    val result: String
) {
    private val num = parts.size
    private val indicies = indicesTierSymbols.take(parts.first().parts.size)

    val firstView = parts.first().parts
        .mapIndexed { i, it -> if (i == 0) it.generalShortView((tierK - 1).toString()) else it.generalShortView(indicies[i - 1]) }
        .intersect()
        .let {
            """\bigcup\limits_{${indicies.dropLast(1).joinToString()}}^{${((tierK - 2)..(tierK - 2 - indicies.size)).toList().joinToString()}}
                |\bigg( $it \bigg)
                |""".trimMargin()
        }
//        .generalShortView(indicesTierSymbols[0])
//        .wrap()

    val stepSequence = if (parts.isNotEmpty())
        (parts.map { it.stepSequence }
            .map { it.mapIndexed { i, case -> if (i < it.lastIndex) case.wrap() else case } }
            .reduce { acc, list -> acc.zip(list).map { listOf(it.first, it.second).union() } } + result)
            .let {
                if (num > 1) listOf(firstView) + it else it
            }
            .run { dropLastWhile { it == last() } + last() }
    else listOf(result)

    fun toLatex() = stepSequence.joinToString(" = ")

    private fun String.wrap() = if (parts.size > 1) "\\Big( $this \\Big)" else this
}

data class SingleCasePart(
    val parts: List<SubCase>,
    val result: String
) {
    val shortFormulaView: String = parts.map { it.shortView }
        .map { it.wrap() }
        .intersect()

    val fullFormulaView: String = parts.map { it.fullView }
        .mapIndexed { i, it ->
            if (parts[i].parts.flatMap { it.elements }.size > 1) it.wrap() else it
        }
        .intersect()

    val fullView: String = parts.map { it.fullUnion }
        .mapIndexed { i, it ->
            if (parts[i].parts.flatMap { it.elements }.size > 1) it.wrap() else it
        }
        .intersect()

    val shortView1: String = parts.map { it.shortUnion }
        .mapIndexed { i, it ->
            if (parts[i].parts.flatMap { it.elements }.size > 1) it.wrap() else it
        }
        .intersect()

    val shortView2: String = parts.map { it.result }
//        .map { it.wrap() }
        .intersect()

    val stepSequence = listOf(
        shortFormulaView,
        fullFormulaView,
        fullView,
        shortView1,
        shortView2,
        result
    )

    fun toLatex() = stepSequence.joinToString(" = ")

    //    private fun String.wrap() = if (parts.size > 1) "\\lbrack $this \\rbrack" else this
    private fun String.wrap() = if (parts.size > 1) "\\Big[ $this \\Big]" else this
}

data class SubCase(
    val arity: String,
    val tier: String,
    val parts: List<SubCasePart>,
    val result: String
) {
    val arityNum get() = arity.toInt()
    val indicies = indicesSymbols.take(arityNum)

    fun generalShortView(tier: String): String = indicies
        .map { "W_{$it}" }
        .intersect()
        .let {
            """\bigcup\limits_{${indicies.joinToString()}}^{
                |${List(arityNum) { parts.last().elements.map { it.i.toInt() }.max().toString() }.joinToString()}
                |}
                |($it)_{$tier}
                |""".trimMargin().replace("\n", "")
        }

    val shortView: String = generalShortView(tier)

    val fullView: String = parts.map { it.intersectView }.map { "($it)_{$tier}" }.union()
    val fullUnion: String = parts.map { it.intersect }.map { if (arityNum > 1) "($it)" else it }.union()
    val shortUnion: String = parts.map { it.result }.union()

    val stepSequence = if (arityNum > 1)
        listOf(
            shortView,
            fullView,
            fullUnion,
            shortUnion,
            result
        )
    else
        listOf(
            shortView,
            fullView,
            shortUnion,
            result
        )

    fun toLatex() = stepSequence.joinToString(" = ")
}

data class SubCasePart(
    val elements: List<Element>,
    val result: String
) {
    val intersect: String = elements.map { it.w }.intersect()
    val intersectView: String = elements.map { it.wView }.intersect()
}


data class Element(
    val i: String,
    val w: String,
    val wView: String = "W_{$i}"
)

private val indicesSymbols = listOf("i", "l", "j", "a", "b", "c", "d", "e", "r", "f", "g", "v")
private val indicesTierSymbols = listOf("p", "h", "y", "u", "z", "q", "t")


fun TPF.ArityCondition.toLatex() = ArityCondition(
    arity,
    tierK,
    fullCase.filter { it.unionOf.isNotEmpty() }.map { it.toLatex(tierK) },
    result.toLatex()
)

fun TPF.ArityCondition.SingleCase.toLatex(tierK: Int) = SingleCase(
    tierK,
    unionOf.map { it.toLatex() },
    result.toLatex()
)

fun TPF.ArityCondition.SingleCasePart.toLatex() = SingleCasePart(
    intersect.map { it.toLatex() },
    result.toLatex()
)


fun TPF.ArityCondition.SubCase.toLatex() = SubCase(
    arity.toString(),
    tier.toString(),
    union.map { it.toLatex() },
    result.toLatex()
)

fun TPF.ArityCondition.SubCasePart.toLatex() = SubCasePart(
    intersect.map { it.toLatex() },
    result.toLatex()
)

fun TPF.ArityCondition.Element.toLatex() = Element(
    i.toLatex(),
    w.toLatex()
)

fun Collection<String>.intersect() = reduceByLatex("\\cap")
fun Collection<String>.union() = reduceByLatex("\\cup")

fun Collection<String>.reduceByLatex(separator: String) =
    if (isNotEmpty())
        reduce { acc, v -> "$acc $separator $v" }
    else emptySetSymbol

