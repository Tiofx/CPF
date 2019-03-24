package buildtask.report

import buildtask.cpf
import report.latex.LatexConverter
import report.latex.TwoAppendixTemplate

val latexConverter get() = LatexConverter(cpf.form())
val template get() = TwoAppendixTemplate(latexConverter.form())


fun main() {
    template.save()
}