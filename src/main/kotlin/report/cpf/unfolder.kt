package report.cpf

import algorithm.*
import guru.nidi.graphviz.engine.Engine
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import report.OperatorNamer
import report.latex.LatexReportTemplate
import report.toPlainString
import java.nio.file.Path


val IOperator.inner: List<IOperator>
    get() = if (this is IGroupOperator) operators else emptyList()

val IOperator.isBaseOperator get() = inner.isEmpty()


class CPFUnfolding(cpfSteps: List<CPF.Iteration>) {
    private val cpfResult = cpfSteps.finalResul()
    private val namer = OperatorNamer(cpfSteps)
    private fun IOperator.toName() = namer.name(this).toPlainString()

    fun toConsole() {
        fun iterate(depth: Int = 0, operator: IOperator = cpfResult) {
            println("$depth: ${operator.toName()}")
            if (!operator.isBaseOperator) println(
                    "${operator.toName()} = ${operator.inner.map { it.toName() }}"
            )

            operator.inner.forEach {
                iterate(depth + 1, it)
            }
        }

        return iterate()
    }

    fun saveAll() {
        savePlain()
        saveAsImage()
    }

    fun savePlain() {
        RESOURCES_FOLDER
                .resolve("assets")
                .resolve("cpf")
                .resolve("plain")
                .resolve("unfolding.txt")
                .toAbsolutePath()
                .toFile()
                .apply {
                    createNewFile()
                    writeText(toGraphviz())
                }
    }

    fun saveAsImage() {
        Graphviz
                .fromString(toGraphviz())
                .engine(Engine.OSAGE)
                .render(Format.PNG)
                .toFile(RESOURCES_FOLDER
                        .resolve("assets")
                        .resolve("cpf")
                        .resolve("unfolding")
                        .toAbsolutePath()
                        .toFile())
    }


    fun toGraphviz(): String {
        fun wrapByGraph(body: String) =
                """
                |graph CPFUnfloding {
                |   layout=osage
                |   style="rounded"
                |   node [shape=circle, fontsize=14, fontname="Times New Roman", margin=".1,.01", fixedsize=true]
                |
                |   ${body.replace("\n", "\n\t")}
                |}
            """.trimMargin()

        fun IOperator.wrapBySubgraph(body: String) =
                """
                |subgraph "cluster_${toName()}" {
                |   label = "${toName()}"
                |
                |   ${body.replace("\n", "\n\t")}
                |}
            """.trimMargin()

        fun iterate(operator: IOperator = cpfResult): String =
                if (operator.isBaseOperator)
                    operator.toName()
                else
                    operator.inner
                            .joinToString("\n") { iterate(it) }
                            .let { operator.wrapBySubgraph(it) }

        return wrapByGraph(iterate())
    }

}

class CPFUnfoldingTemplate : LatexReportTemplate() {
    override val preamble: String
        get() = super.preamble + "\n\\pagenumbering{gobble}"

    override val documentBody: String
        get() = """
\begin{figure}[H]
    \centering
    \includegraphics[width=\textwidth,height=\textheight,keepaspectratio]{unfolding.png}
\end{figure}
        """.trimIndent()

    override fun Path.configResourcesPath() = resolve("assets").resolve("cpf").resolve("unfolding.tex")
}