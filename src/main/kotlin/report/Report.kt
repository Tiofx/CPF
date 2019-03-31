package report

import report.latex.LatexReportTemplate
import java.nio.file.Path

class Report(val startPage: Int) : LatexReportTemplate() {

    override val preamble: String
        get() = super.preamble + """
\usepackage{fancyhdr}

\pagestyle{fancy}
\fancyhf{}
\fancyheadoffset{0cm}
\renewcommand{\headrulewidth}{0pt}
\renewcommand{\footrulewidth}{0pt}
\fancyhead[R]{\thepage}
\fancypagestyle{plain}{%
\fancyhf{}%
\fancyhead[R]{\thepage}%
}

\usepackage[final]{pdfpages}
\setcounter{page}{$startPage}

        """.trimIndent()

    override val documentBody: String
        get() = """
\includepdf[offset=0mm 8mm, pages=-,pagecommand={}]{assets/report.pdf}
\includepdf[offset=0mm 8mm, pages=-,pagecommand={}]{assets/cpf/report.pdf}
        """.trimIndent()

    override fun Path.configResourcesPath() = resolve("report.tex")
}