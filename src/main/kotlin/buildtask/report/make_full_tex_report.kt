package buildtask.report

import report.Report

val fullTemplate get() = Report()


fun main() {
    fullTemplate.save()
}