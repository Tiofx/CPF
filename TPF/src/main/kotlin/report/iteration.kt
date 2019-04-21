package iteration

import algorithm.CashedTPF
import algorithm.Program
import algorithm.RESOURCES_FOLDER
import algorithm.TPF
import report.toLatex


fun main() {
    val iterations = CashedTPF(Program.from()).form(8)

    RESOURCES_FOLDER.resolve("draft_report.txt").toFile().run {
        createNewFile()
        writeText(iterations.asDraftString())
    }

    RESOURCES_FOLDER.resolve("report_full_formula_output_without_last.txt").toFile().run {
        createNewFile()
        writeText(iterations.dropLast(1).asFullFormulaOutputDraft())
    }

    RESOURCES_FOLDER.resolve("report_full_formula_output_last_only.txt").toFile().run {
        createNewFile()
        writeText(iterations.takeLast(1).asFullFormulaOutputDraft())
    }
}


fun List<TPF.Iteration>.asDraftString() = mapIndexed { index, iteration ->
    """
========================= Ярус $index ===============================
--- Множества модели процесса
${iteration.processModel.toLatex(index)}
---------------------------------------------------------------------

-- Операции по арностям
${iteration.operationByArity.toReport()}
---------------------------------------------------------------------

-- Условия проверки арности
${iteration.arityCondition.toLatex()}
---------------------------------------------------------------------

-- Текущий ярус:
${iteration.tier.toLatexAsOperationSequence()}
---------------------------------------------------------------------

--Множества W
${iteration.tierW.toLatex(index)}
---------------------------------------------------------------------
======================================================================



    """.trimIndent()
}.joinToString("\n")

fun List<TPF.Iteration>.asFullFormulaOutputDraft() = mapIndexed { index, iteration ->
    """
Ярус $index:\\
${iteration.arityCondition.toLatex(false)}
======================================================================




    """.trimIndent()
}.joinToString("\n")
    .let { "\\begin{center} ПРИЛОЖЕНИЕ Б \\end{center}\\\\ \\vspace{5mm}\n$it" }


fun Map<Int, List<Int>>.toReport() = toList()
    .sortedBy { it.first }
    .joinToString("\n\n") { "${it.first}: ${it.second.toLatexAsOperationSequence()}" }


fun List<Set<Int>>.toLatex(tierK: Int, isCondensed: Boolean = false) = mapIndexed { index, set ->
    set.toLatex().let { "\\Big( W_{${index + 1}} \\Big)_{$tierK} &= $it" }
}
    .joinToString(if (isCondensed) ",\\  & " else "\\\\ \n")
    .let {
        """
\begin{align*}
    $it
\end{align*}
    """.trimIndent()
    }


fun TPF.ProcessModel.toLatex(tier: Int) =
    """
^0A_{$tier} &= ${notReady.toLatexAsOperationSequence()} \\
^*A_{$tier} &= ${ready.toLatexAsOperationSequence()} \\
^+A_{$tier} &= ${switchOn.toLatexAsOperationSequence()} \\
^pA_{$tier} &= ${worked.toLatexAsOperationSequence()} \\
^-A_{$tier} &= ${switchOff.toLatexAsOperationSequence()} \\
        """.trimIndent()
        .let {
            """
\begin{align*}
    $it
\end{align*}
    """.trimIndent()
        }

private fun Collection<Int>.toLatexAsOperationSequence() = map { it + 1 }
    .map { "a_{$it}" }
    .toSet()
    .toLatex()