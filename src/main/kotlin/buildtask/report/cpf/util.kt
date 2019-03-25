package buildtask.report.cpf

import buildtask.cpf
import report.cpf.CPFFullReport
import report.cpf.CPFUnfolding
import report.cpf.CPFUnfoldingTemplate
import report.cpf.iteration.CPFItreationsGraphSaver
import report.cpf.iteration.IterationsGraphReport

val saver get() = CPFItreationsGraphSaver(cpf.form())
val texReportMaker get() = IterationsGraphReport(cpf.form().size)

val unfoldingReportMaker get() = CPFUnfoldingTemplate()
val unfolder get() = CPFUnfolding(cpf.form())

val fullReport get() = CPFFullReport()
