package buildtask.report.cpf

import buildtask.cpf
import report.cpf.CPFUnfolding
import report.cpf.iteration.CPFItreationsGraphSaver
import report.cpf.iteration.IterationsGraphReport

val saver get() = CPFItreationsGraphSaver(cpf.form())
val unfolder get() = CPFUnfolding(cpf.form())
val texReportMaker get() = IterationsGraphReport(cpf.form().size)