package graph

import algorithm.*
import report.OperatorNamer
import report.toPlainString


val IOperator.inner: List<IOperator>
    get() = if (this is IGroupOperator) operators else emptyList()

val IOperator.isBaseOperator get() = inner.isEmpty()

fun main() {
    val program = programText
        .run { prepareToLatex() }
        .map(::Operator)
        .let { CashedProgram(it) }


    val cpf = CPF(program)
    val result = cpf.form()
    val cpfUnfolder = CPFUnfolding(result)
    println(cpfUnfolder.toGraph())
}

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

    fun toGraph(): String {
        fun wrapByGraph(body: String) =
            """
                |graph CPFUnfloding {
                |   layout=osage;
                |   style="rounded"
                |   node [shape=circle, fontsize=14, fontname="Times New Roman", margin=".1,.01"]
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

        fun iterate(depth: Int = 0, operator: IOperator = cpfResult): String =
            if (operator.isBaseOperator)
                operator.toName()
            else
                operator.inner
                    .joinToString("\n") { iterate(depth + 1, it) }
                    .let { operator.wrapBySubgraph(it) }

        return wrapByGraph(iterate())
    }

}