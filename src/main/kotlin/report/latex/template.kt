package report.latex

import algorithm.RESOURCES_FOLDER
import java.nio.file.Path
import kotlin.math.min

class TwoAppendixTemplate(val content: List<LatexConverter.Iteration>) : LatexReportTemplate() {
    override val documentBody: String
        get() = """
            |${AppendixA().documentBody}
            |${AppendixB().documentBody}
        """.trimMargin()

    inner class AppendixA : ByIterationTemplate(content) {

        override val documentBody: String
            get() = """
                |\begin{center} ПРИЛОЖЕНИЕ A \end{center}\\
                |${super.documentBody}
            """.trimMargin()

        override fun toLatex(iteration: LatexConverter.Iteration) = iteration.run {
            """
            |$relations
            |%
            |$cpfChecks
            |%
            |${iterationType(iteration)}
        """.trimMargin()
        }

    }

    inner class AppendixB : LatexReportTemplate() {
        private val singleOperators = content.first().program.operators

        override val documentBody: String
            get() = """
                |\begin{center} ПРИЛОЖЕНИЕ Б \end{center}\\
                |\begin{math}\breakingcomma
                |${singleOperators.map { it.toLatex() }.joinToString(LatexConverter.singleLineBreak)}
                |\end{math}
            """.trimMargin()
    }
}

open class ByIterationTemplate(val content: List<LatexConverter.Iteration>) : LatexReportTemplate() {
    companion object {
        val MAX_NUMBER_OF_CPF_CHECK = 5
    }

    override val documentBody: String =
            content
                    .dropLast(1)
                    .map { toLatex(it) }
                    .mapIndexed { i, it ->
                        """
                        |\begin{center}\huge Итерация №${i + 1} \end{center}\\
                        |$it${LatexConverter.singleLineBreak}
                        |\newpage
                        """.trimMargin()
                    }
                    .joinToString(LatexConverter.singleLineBreak)


    protected open fun toLatex(iteration: LatexConverter.Iteration) = iteration.run {
        """
        |$sets
        |%
        |$relations
        |%
        |$cpfChecks
        |%
        |${iterationType(iteration)}
    """.trimMargin()
    }

    protected val LatexConverter.Iteration.sets
        get() = """
        |Множества упоминаемых, изменяемых, выходных и входных переменных: \\
        |\begin{math}\breakingcomma
        |${program.toLatex()}
        |\end{math}\\
    """.trimMargin()

    protected val LatexConverter.Iteration.relations
        get() = """
        |Отношения между операторами: \\ \newline
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
        |\end{math}\\
        """.trimMargin()


    protected

    fun iterationType(iteration: LatexConverter.Iteration) = iteration.run {
        (if (parallelIteration) "паралллельный" else "последовательный")
                .let {
                    """
            |На текущей итерации был выделен $it групповой оператор $$resultOfIteration$${LatexConverter.singleLineBreak}
        """.trimMargin()
                }
    }

    private fun String.wrapInMath() = "\\text{$this}"
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
