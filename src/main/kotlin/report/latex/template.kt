package report.latex

import algorithm.ProgramText
import algorithm.RESOURCES_FOLDER
import algorithm.prepareToLatex
import algorithm.programText
import java.nio.file.Path
import kotlin.math.min

class TwoAppendixTemplate(val content: List<LatexConverter.Iteration>) : LatexReportTemplate() {

    override val preamble: String
        get() = super.preamble + "\\pagenumbering{gobble}\n"

    override val documentBody: String
        get() = """
            |${AppendixA().documentBody}
            |${AppendixB().documentBody}
        """.trimMargin()


    inner class AppendixA : LatexReportTemplate() {

        private val singleOperators = content.first().program.operators

        private val programTable = programText.prepareToLatex().toTable()

        private fun ProgramText.toTable(): String {
            data class OperatorFullInfo(
                    val name: String,
                    val content: String,
                    val C: String,
                    val R: String
            ) {
                fun toLatex() =
                        (if (content.startsWith("A^T")) copy(
                                content = content.split(";")
                                        .joinToString(";$ \\\\ $")
                                        .let { "$$it$" }
                                        .let { """\begin{tabular}[x]{@{}}$it\end{tabular}""" },

                                R = R.split(",")
                                        .mapIndexed { i, s -> if (i > 1 && (i + 1) % 3 == 0) "$s$ \\\\ $" else s }
                                        .joinToString()
                                        .let { "$$it$" }
                                        .let { """\begin{tabular}[x]{@{}}$it\end{tabular}""" }
                        ) else this)
                                .run { listOf(name, content, C, R) }
                                .map { "$$it$" }
            }

            return mapIndexed { i, s -> singleOperators[i].run { OperatorFullInfo(name, s, C.toLatex(), R.toLatex()) } }
                    .map(OperatorFullInfo::toLatex)
                    .joinToString(""" \\ \hline""" + "\n") { it.joinToString(" &") }
                    .let {
                        """
            \setlength{\tabcolsep}{15pt}
            \renewcommand{\arraystretch}{1.5}

            \begin{tabular}{|p{0.03\linewidth}|p{0.25\linewidth}|p{0.2\linewidth}|p{0.2\linewidth}|}
                \hline
                S & Оператор & C(S) & R(S) \\ \hline
                $it
                \\ \hline
            \end{tabular}
                                        """.trimIndent()
                    }
        }

        override val documentBody: String
            get() = """
                |\begin{center} ПРИЛОЖЕНИЕ А \end{center}\\ \vspace{5mm}
                |
                |Таблица А.1 – Представление программы в виде последовательности операторов\\
                |$programTable
            """.trimMargin()
    }


    inner class AppendixB : ByIterationTemplate(content) {

        override val documentBody: String
            get() = """
                |\begin{center} ПРИЛОЖЕНИЕ Б \end{center}\\
                |${super.documentBody}
            """.trimMargin()


    }
}

open class ByIterationTemplate(val content: List<LatexConverter.Iteration>) : LatexReportTemplate() {
    companion object {
        val MAX_NUMBER_OF_CPF_CHECK = 5
    }

    override val preamble: String
        get() = super.preamble + "\\pagenumbering{gobble}\n"

    override val documentBody: String =
            content
                    .dropLast(1)
                    .mapIndexed { i, it -> if (i == 0) toLatexFirstIteration(it) else toLatex(it) }
                    .mapIndexed { i, it ->
                        """
                        |\\
                        |\begin{center} Итерация №${i + 1} \end{center}
                        |$it
                        """.trimMargin()
                    }
                    .joinToString("\n")

    protected fun toLatexFirstIteration(iteration: LatexConverter.Iteration) =
            iteration.run {
                """
                    |Отношения между операторами: \\
                    |Отношения сильной зависимости: \\ \newline
                    |$relations
                    |%
                    |$cpfChecks
                    |%
                    |${iterationType(iteration)}
                """.trimMargin()
            }

    protected open fun toLatex(iteration: LatexConverter.Iteration) =
            iteration.run {
                """
                    |Отношения между операторами: \\
                    |${if (matrices.strongDependencyRelations.isNotBlank())
                    "Отношения сильной зависимости с выделенным на прошлой итерации групповым оператором: \\\\ \\newline"
                else
                    "Выделенный на прошлой итерации групповой оператор не образует сильных связей."
                }
                    |$relations
                    |%
                    |${iterationType(iteration)}
                """.trimMargin()
            }


    protected val LatexConverter.Iteration.relations
        get() =
            """
                |${matrices.toLatex()}\\ \newline
            """.trimMargin()

    protected val LatexConverter.Iteration.cpfChecks
        get() = """
        |Проверка условия приводимости программы к ППФ (${min(
                MAX_NUMBER_OF_CPF_CHECK,
                cpfCheck.size
        )} из ${cpfCheck.size}): \\
        |\begin{math}\breakingcomma
        |${cpfCheck.take(MAX_NUMBER_OF_CPF_CHECK).toLatex()}
        |\end{math}
        |${LatexConverter.singleLineBreak}
        """.trimMargin()


    protected fun iterationType(iteration: LatexConverter.Iteration) = iteration.run {
        (if (parallelIteration) "паралллельный" else "последовательный")
                .let {
                    """
                        |На текущей итерации был выделен $it групповой оператор $$resultOfIteration$
                    """.trimMargin()
                }
    }

}

class FullLatexGenerator(content: List<LatexConverter.Iteration>) : LatexReportTemplate() {
    override val documentBody =
            """\breakingcomma
    \tiny
    ${content.toLatex12()}
    \interdisplaylinepenalty=10000
    \normalsize
    \begin{math}\breakingcomma
    ${content.toLatex()}
    \end{math}
    """.trimIndent()

    fun List<LatexConverter.Iteration>.toLatex() = map { it.toLatex() }.joinToString(LatexConverter.singleLineBreak)

    fun LatexConverter.Iteration.toLatex(): String = """
            |${program.toLatex()}
            |${cpfCheck.joinToString(LatexConverter.doubleLineBreak) { it.toLatex() }}
        """.trimMargin()

    fun List<LatexConverter.Iteration>.toLatex1() =
            map { it.program.names.toLatex() to "${it.operatorsGroup.toLatex()} \\to ${it.groupedOperator}" }
                    .map { it.first + LatexConverter.singleLineBreak + it.second + LatexConverter.singleLineBreak }

    fun List<LatexConverter.Iteration>.toLatex2() = map { it.matrices }
            .map {
                listOf(
                        it.strongDependencyRelations,
                        it.weekIndependencyMatrix
                )
                        .joinToString(LatexConverter.singleLineBreak)
            }

    fun List<LatexConverter.Iteration>.toLatex12() = toLatex1().zip(toLatex2())
            .map { "$" + it.first + "$" + LatexConverter.singleLineBreak + it.second }
            .joinToString(LatexConverter.singleLineBreak)
}


abstract class LatexReportTemplate {
    protected val report by lazy {
        """
$preamble

\begin{document}
    $documentBody
\end{document}
            """.trimIndent()
    }

    protected open val preamble by lazy {
        """
\documentclass[a4paper,14pt]{article}
\usepackage{geometry}
\geometry{letterpaper}
\usepackage{graphicx}
\usepackage{amsmath}

\usepackage{geometry}
 \geometry{
 a4paper,
 total={170mm,257mm},
 left=25mm,
 right=10mm,
 top=20mm,
 bottom=20mm,
 }

\usepackage[utf8]{inputenc}
\usepackage[T1,T2A]{fontenc}
\usepackage{amssymb}
\usepackage{graphicx}

\usepackage{fontspec}
\setmainfont{Times New Roman}

\usepackage{breqn}
\setkeys{breqn}{breakdepth={1}}
\newcommand{\breakingcomma}{%
  \begingroup\lccode`~=`,
  \lowercase{\endgroup\expandafter\def\expandafter~\expandafter{~\penalty0 }}}

\makeatletter
\def\bbordermatrix#1{\begingroup \m@th
  \@tempdima 4.75\p@
  \setbox\z@\vbox{%
    \def\cr{\crcr\noalign{\kern2\p@\global\let\cr\endline}}%
    \ialign{${'$'}##${'$'}\hfil\kern2\p@\kern\@tempdima&\thinspace\hfil${'$'}##${'$'}\hfil
      &&\quad\hfil${'$'}##${'$'}\hfil\crcr
      \omit\strut\hfil\crcr\noalign{\kern-\baselineskip}%
      #1\crcr\omit\strut\cr}}%
  \setbox\tw@\vbox{\unvcopy\z@\global\setbox\@ne\lastbox}%
  \setbox\tw@\hbox{\unhbox\@ne\unskip\global\setbox\@ne\lastbox}%
  \setbox\tw@\hbox{${'$'}\kern\wd\@ne\kern-\@tempdima\left[\kern-\wd\@ne
    \global\setbox\@ne\vbox{\box\@ne\kern2\p@}%
    \vcenter{\kern-\ht\@ne\unvbox\z@\kern-\baselineskip}\,\right]${'$'}}%
  \null\;\vbox{\kern\ht\@ne\box\tw@}\endgroup}
\makeatother
        """.trimIndent()
    }

    abstract val documentBody: String

    fun save() {
        save(report)
    }

    protected open fun Path.configResourcesPath(): Path = resolve("assets").resolve("report.tex")

    private fun save(result: String) {
        RESOURCES_FOLDER
                .configResourcesPath()
                .toAbsolutePath().toFile().apply {
                    createNewFile()
                    writeText(result)
                }
    }
}
