package report.cpf

import report.latex.LatexReportTemplate
import java.nio.file.Path


class CPFFullReport : LatexReportTemplate() {
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
        """.trimIndent()

    override val documentBody: String
        get() = """
\includepdf[pages=-,pagecommand={}]{iterations.pdf}
\includepdf[pages=-,pagecommand={}]{unfolding.pdf}
\includepdf[pages=-,pagecommand={}]{execution_graph.pdf}
        """.trimIndent()

    override fun Path.configResourcesPath() = resolve("assets")
            .resolve("cpf")
            .resolve("report.tex")
}