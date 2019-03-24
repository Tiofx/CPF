package report.cpf.iteration

import algorithm.*


fun main() {
    val program = programText
            .run { prepareToLatex() }
            .map(::Operator)
            .let { CashedProgram(it) }


    val cpf = CPF(program)
    val result = cpf.form()

    IterationsGraphReport(result.size).save()
}