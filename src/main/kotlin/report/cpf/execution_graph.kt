package report.cpf

import algorithm.*
import guru.nidi.graphviz.engine.Engine
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import report.OperatorNamer
import report.latex.LatexReportTemplate
import report.toPlainString
import java.nio.file.Path


class CPFExecutionGraph(cpfSteps: List<CPF.Iteration>) {
    private val cpfResult = cpfSteps.finalResul()
    private val namer = OperatorNamer(cpfSteps)
    private fun IOperator.toName() = namer.name(this).toPlainString()

    fun savePlain() {
        RESOURCES_FOLDER
                .resolve("assets")
                .resolve("cpf")
                .resolve("plain")
                .resolve("execution_graph.txt")
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
//                .engine(Engine.DOT)
                .render(Format.PNG)
                .toFile(RESOURCES_FOLDER
                        .resolve("assets")
                        .resolve("cpf")
                        .resolve("execution_graph")
                        .toAbsolutePath()
                        .toFile())
    }


    fun toGraphviz(): String {
        fun IOperator.wrapBySubgraph(body: String) =
                """
                |subgraph "cluster_${toName()}" {
                |   label = "${toName()}"
                |    rankdir = BT
                |
                |   ${body.replace("\n", "\n\t")}
                |}
            """.trimMargin()

        fun String.wrapByCluster() =
                """
                    |subgraph "cluster_${hashCode()}" {
                    |   label = ""
                    |   style = invis
                    |   margin = 0
                    |
                    |   $this
                    |}
                """.trimMargin()

        fun wrapByGraph(body: String) =
                """
                |digraph CPFExecutionGraph {
                |   node [shape=circle, fontsize=14, fontname="Times New Roman", margin=".1,.01", fixedsize=true]
                |
                |   ${order()}
                |   ${body.replace("\n", "\n\t").wrapByCluster()}
                |}
            """.trimMargin()


        fun IOperator.wrapSimpleOperator() =
                """
                    |subgraph "cluster_${hashCode()}" {
                    |   label = ""
                    |   margin = 0
                    |   style = invis
                    |
                    |   ${toName()}
                    |}
                """.trimMargin()

        fun iterate(operator: IOperator = cpfResult): String =
                if (operator.isBaseOperator)
                    operator.wrapSimpleOperator()
                else
                    operator.inner
                            .reversed()
                            .joinToString("\n") { iterate(it) }
                            .let { operator.wrapBySubgraph(it) }

        return wrapByGraph(iterate())
    }

    fun order(operator: IOperator = cpfResult): String {
        fun IOperator.wrap(): String {
            fun IOperator._wrap(): String {
                fun IOperator.wrapParallelOperator() = inner.joinToString(", ") { it._wrap() }
                fun IOperator.wrapSequentialOperator() =  inner.last()._wrap()

                return when {
                    this.isBaseOperator -> this.toName()

                    this is GroupOperator ->
                        when (type) {
                            GroupOperator.Type.PARALLEL -> wrapParallelOperator().let { "{$it}" }
                            GroupOperator.Type.SEQUENTIAL -> wrapSequentialOperator()
                        }

                    else -> throw IllegalArgumentException("There is new type of IOperator!")
                }
            }

            return if (operator is GroupOperator && operator.type == GroupOperator.Type.SEQUENTIAL)
                inner.joinToString(" -> ") { it._wrap() }
            else
                ""
        }


        val currentResult = operator.wrap()

        return operator.inner
                .map { order(it) }
                .plus(currentResult)
                .filter { it.isNotBlank() }
                .joinToString("\n")
                .trim()
    }


}


class CPFExecutionGraphTemplate : LatexReportTemplate() {
    override val preamble: String
        get() = super.preamble + "\n\\pagenumbering{gobble}"

    override val documentBody: String
        get() = """
\begin{figure}[H]
    \centering
    \includegraphics[width=\textwidth,height=\textheight,keepaspectratio]{execution_graph.png}
\end{figure}
        """.trimIndent()

    override fun Path.configResourcesPath() = resolve("assets").resolve("cpf").resolve("execution_graph.tex")
}