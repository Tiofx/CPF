import algorithm.*
import report.latex.LatexConverter
import report.latex.TwoAppendixTemplate

fun main() {
    graph.main()

    val program = programText
            .run { prepareToLatex() }
            .map(::Operator)
            .let { CashedProgram(it) }


    val cpf = CPF(program)
    val result = cpf.form()

    val latex = LatexConverter(result).form()
    TwoAppendixTemplate(latex).save()
}
