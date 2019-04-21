package buildtask

import algorithm.*

val program = programText
        .run { prepareToLatex() }
        .map(::Operator)
        .let { CashedProgram(it) }

val cpf get() = CPF(program)