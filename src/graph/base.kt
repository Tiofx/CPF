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
    CPFUnfolding(result).iterate()
}

class CPFUnfolding(fpfSteps: List<CPF.Iteration>) {
    private val fpfResult = fpfSteps.finalResul()
    private val namer = OperatorNamer(fpfSteps)
    private fun IOperator.toName() = namer.name(this).toPlainString()

    fun iterate() {
        fun iterate(depth: Int = 0, operator: IOperator = fpfResult) {
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
}