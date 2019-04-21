package appendixA

import algorithm.*
import report.toLatex
import java.nio.file.Path

fun main() {
    AppendixA(Program.from(programText).operators, programText).save()
}

class AppendixA(val operations: List<Operation>, val programText: ProgramText) : LatexReportTemplate() {

    private fun List<Operation>.toTable(): String = mapIndexed { i, it -> it.toLatex(programText[i]) }
        .map { it.toList().mapIndexed { i, it -> if (i != 2) "$$it$" else it } }
        .joinToString(""" \\ \hline""" + "\n") { it.joinToString(" &") }
        .let {
            """
        \setlength{\tabcolsep}{10pt}
        \renewcommand{\arraystretch}{1.4}

        \breakingcomma
        \begin{tabular}{|c|p{0.16\linewidth}|c|p{0.14\linewidth}|c|p{0.1\linewidth}|p{0.05\linewidth}|p{0.025\linewidth}|}
            \hline
            ${listOf(
                "\\textnumero",
                "Операция",
                "\\theta_j",
                "in\\,a_j",
                "out\\,a_j",
                "w_j",
                "|in\\,a_j|",
                "|w_j|"
            )
                .mapIndexed { i, it -> if (i > 1) "$$it$" else it }
                .joinToString(" &")}\\ \hline
            $it
            \\ \hline
        \end{tabular}
                                    """.trimIndent()
        }

    override val documentBody: String
        get() = """
                |\begin{center} ПРИЛОЖЕНИЕ А \end{center}\\ \vspace{5mm}
                |
                |Таблица А.1 – Представление программы в виде последовательности операций\\
                |${operations.toTable()}
            """.trimMargin()
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

\pagenumbering{gobble}

        """.trimIndent()
    }

    abstract val documentBody: String

    fun save() {
        save(report)
    }

    protected open fun Path.configResourcesPath(): Path = resolve("report.tex")

    private fun save(result: String) {
        RESOURCES_FOLDER
            .configResourcesPath()
            .toAbsolutePath().toFile().apply {
                createNewFile()
                writeText(result)
            }
    }
}
