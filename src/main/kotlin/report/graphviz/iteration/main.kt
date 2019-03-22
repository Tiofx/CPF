package report.graphviz.iteration

import algorithm.*


fun main() {
    val program = programText
            .run { prepareToLatex() }
            .map(::Operator)
            .let { CashedProgram(it) }


    val cpf = CPF(program)
    val result = cpf.form()

    val saver = CPFItreationsGraphSaver(result)
    saver.save()
}