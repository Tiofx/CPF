package report.latex

import algorithm.C
import algorithm.CPF
import algorithm.CPFChecker
import algorithm.Program
import algorithm.R
import algorithm.RelationsMatrix
import algorithm.indices
import report.OperatorName
import report.OperatorNamer
import report.latex.LatexConverter.Companion.doubleLineBreak
import algorithm.resultOfGroupOperators


class LatexConverter(val fpfResults: List<CPF.Iteration>) {
    private val namer = OperatorNamer(fpfResults)

    companion object {
        val singleLineBreak = " \\\\ \n"
        val doubleLineBreak = " \\\\ \\newline \n%\n"
        val tripleLineBreak = " \\\\ \\\\ \\\\ \n%\n"
    }

    fun form() = fpfResults.mapIndexed { i, iteration ->
        Iteration(
            ProgramLatex(iteration.program),
            RelationsMatrixLatex(iteration.allRelationsMatrices, iteration.program),
            iteration.fpfCheck.map { CPFCheck(iteration.program, it) },
            iteration.isParallel,
            iteration.groupedOperators.map { namer.name(iteration.program, it).toLatex() }.toSet(),
            fpfResults.resultOfGroupOperators(i)?.let { namer.name(it).toLatex() } ?: ""
//            if (i != fpfResults.core.getLastIndex)
//                namer.name(fpfResults[i + 1].program, iteration.groupedOperators.first).toLatex()
//            else
//                ""
        )
    }


    inner class Iteration(
        val program: ProgramLatex,
        val matrices: RelationsMatrixLatex,
        val fpfCheck: List<CPFCheck>,
        val parallelIteration: Boolean,
        val operatorsGroup: Set<String>,
        val groupedOperator: String
    ) {
        val resultOfIteration get() = "$groupedOperator = ${operatorsGroup.toLatex()}"
    }

    inner class ProgramLatex(val operators: List<OperatorInfo>) {

        val names get() = operators.map { it.name }.toSet()

        constructor(program: Program) : this(program.indices.map { OperatorInfo(program, it) })

        fun toLatex(): String = operators.map { it.toLatex() }.joinToString(doubleLineBreak)
    }

    inner class OperatorInfo(
        val name: String,
        val C: Set<String>,
        val R: Set<String>,
        val I: Set<String>,
        val O: Set<String>
    ) {
        constructor(program: Program, i: Int) : this(
            namer.name(program, i).toLatex(),
            program.C(i),
            program.R(i),
            program.I(i),
            program.O(i)
        )

        fun toLatex() = listOf(
            name,
            C._toLatex("C"),
            R._toLatex("R"),
            I._toLatex("I"),
            O._toLatex("O")
        ).joinToString(singleLineBreak)

        private fun Set<String>._toLatex(setName: String): String = toLatex(fullName(setName))
        private fun fullName(setName: String) = "$setName($name)"
    }

    inner class RelationsMatrixLatex(
        val strongDependencyMatrix: String,
        val weekDependencyMatrix: String,
        val strongIndependencyMatrix: String,
        val weekIndependencyMatrix: String
    ) {

        constructor(matrices: RelationsMatrix, program: Program) : this(matrices, header(program))

        constructor(matrices: RelationsMatrix, header: List<String>) : this(
            "SD = ${matrices.strongDependencyMatrix.toLatex(header, header)}",
            "WD = ${matrices.weekDependencyMatrix.toLatex(header, header)}",
            "SI = ${matrices.strongIndependencyMatrix.toLatex(header, header)}",
            "C = ${matrices.weekIndependencyMatrix.toLatex(header, header)}"
        )

        fun toLatex() = listOf(
            strongDependencyMatrix,
            weekDependencyMatrix,
            weekIndependencyMatrix,
            strongIndependencyMatrix
        ).joinToString(tripleLineBreak)

    }

    private fun header(program: Program) = namer.names(program).map(OperatorName::toLatex)

    inner class CPFCheck(
        val Si: String,
        val Sj: String,
        val Sk: String,
        val Ei: Set<String>,
        val Ej: Set<String>,
        val N1: Set<String>,
        val N2: Set<String>,
        val Ek: Set<String>,
        val isSkInN2: Boolean,
        val isN1InEk: Boolean,
        val canBeBringToCPF: Boolean
    ) {
        constructor(program: Program, r: CPFChecker.Result) : this(
            namer.name(program, r.i).toLatex(),
            namer.name(program, r.j).toLatex(),
            namer.name(program, r.k).toLatex(),
            r.Ei.map { namer.name(program, it).toLatex() }.toSet(),
            r.Ej.map { namer.name(program, it).toLatex() }.toSet(),
            r.N1.map { namer.name(program, it).toLatex() }.toSet(),
            r.N2.map { namer.name(program, it).toLatex() }.toSet(),
            r.Ek.map { namer.name(program, it).toLatex() }.toSet(),
            r.isSkInN2,
            r.isN1InEk,
            r.canBeBringToCPF
        )

        private val Boolean.asText
            get() = (if (this) "выполняется" else "не выполняется").let { "\\text{$it}" }

        fun toLatex(): String {
            class Temp(
                val Si: String,
                val Sj: String,
                val Sk: String,
                val Ei: String,
                val Ej: String,
                val N1: String,
                val N2: String,
                val Ek: String
            )

            val temp = Temp(
                Si, Sj, Sk,
                Ei.toLatex("E($Si)"),
                Ej.toLatex("E($Sj)"),
                N1.toLatex("N_1($Si, $Sj)"),
                N2.toLatex("N_2($Si, $Sj)"),
                Ek.toLatex("E($Sk)")
            )

            return temp.run {
                """
                    |1) $Si \nrightarrow $Sj
                    |2) $Sk
                    |$Ei
                    |$Ej
                    |$N1
                    |$N2
                    |$Ek
                    |$Sk \in N_2($Si, $Sj) - ${isSkInN2.asText}
                    |N_1($Si, $Sj) \subseteq E($Sk) - ${isN1InEk.asText}
                    |${canBeBringToCPF.asText.capitalize()}
                """.trimMargin()
            }
                .split("\n")
                .joinToString(singleLineBreak)
        }
    }
}

fun List<LatexConverter.CPFCheck>.toLatex() = joinToString(doubleLineBreak) { it.toLatex() }

//fun List<LatexConverter.Iteration>.toLatex() = map { it.toLatex() }.joinToString(singleLineBreak)


