package report.cpf

import report.latex.LatexReportTemplate
import java.nio.file.Path


class CPFFullReport : LatexReportTemplate() {
    override val preamble: String
        get() = super.preamble + """

\usepackage[final]{pdfpages}
\pagenumbering{gobble}

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