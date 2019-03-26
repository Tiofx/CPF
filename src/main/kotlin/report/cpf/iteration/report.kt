package report.cpf.iteration

import report.latex.LatexReportTemplate
import java.nio.file.Path

class IterationsGraphReport(val iterationNumber: Int) : LatexReportTemplate() {

    companion object {
        private const val WIDTH = 50
    }

    override val preamble: String
        get() =
            """
\documentclass[a4paper,14pt]{article}
\usepackage{geometry}
\geometry{letterpaper}
\usepackage{graphicx}
\usepackage{amsmath}
\usepackage{calc}
\usepackage{geometry}
 \geometry{
 a4paper,
 total={170mm,257mm},
 left=25mm,
 right=10mm,
 top=20mm,
 bottom=20mm,
 }

\usepackage{amssymb}
\usepackage[export]{adjustbox}

\vtop{%
  \vskip0pt
  \hbox{%
    \includegraphics{figure}%
  }%
}

\newsavebox{\mybox}
\let\oldincludegraphics\includegraphics
\xdef\maxwidth{0.9\textwidth}
\renewcommand{\includegraphics}[2][]{%
  \savebox{\mybox}{%
    \hbox{\oldincludegraphics[#1]{#2}}}%
\ifdim\wd\mybox>\maxwidth
  \oldincludegraphics[width=$WIDTH,keepaspectratio]{#2}%
\else
  \oldincludegraphics[#1]{#2}%
\fi}


\ifpdf % We're generating a pdf
    \usepackage[pdftex]{color,graphicx}
    \pdfpagewidth=\paperwidth
    \pdfpageheight=\paperheight
    \usepackage{thumbpdf}
    %\pdfcompresslevel=9
\else
    \usepackage[dvips]{graphicx}
\fi

\pagenumbering{gobble}
            """.trimIndent()

    override val documentBody: String
        get() =
            (1..iterationNumber).map { include("$it.png") }
                    .joinToString("\n")


    protected fun include(fileName: String) = "\\raisebox{1ex-\\height}{${includeGraphics(fileName)}}"

    protected fun includeGraphics(fileName: String) =
            "\\includegraphics[trim=5mm 5mm 5mm 5mm,clip,width=$WIDTH, height=\\textheight,keepaspectratio]{iterations/$fileName} "

    override fun Path.configResourcesPath() =
            resolve("assets")
                    .resolve("cpf")
                    .resolve("iterations.tex")
}