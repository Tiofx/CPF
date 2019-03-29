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
                |   style="rounded"
                |   node [shape=circle, fontsize=14, fontname="Times New Roman", margin=".1,.01", fixedsize=true]
                |   edge[style=invis]
                |
                |   ${order()}
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
                inner.joinToString(" -- ") { it._wrap() }
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

class CPFUnfoldingTemplate : LatexReportTemplate() {
    override val preamble: String
        get() = super.preamble + """

\pagenumbering{gobble}
\usepackage{rotating}
\usepackage{caption}
\captionsetup[figure]{labelformat=empty}

        """.trimIndent()

    override val documentBody: String
        get() = """
\begin{sidewaysfigure}
    \centering
    \includegraphics[width=\textwidth,height=\textheight-20,keepaspectratio]{unfolding.png}
    \caption{Рисунок Б.1 – Представление ППФ}
\end{sidewaysfigure}
        """.trimIndent()

    override fun Path.configResourcesPath() = resolve("assets").resolve("cpf").resolve("unfolding.tex")
}