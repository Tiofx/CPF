package buildtask.report.cpf

import buildtask.cpf
import report.graphviz.CPFUnfolding
import report.graphviz.iteration.CPFItreationsGraphSaver
import report.graphviz.iteration.IterationsGraphReport

val saver get() = CPFItreationsGraphSaver(cpf.form())
val unfolder get() = CPFUnfolding(cpf.form())
val texReportMaker get() = IterationsGraphReport(cpf.form().size)