package buildtask.report

import algorithm.RESOURCES_FOLDER
import report.Report


private val START_PAGE = RESOURCES_FOLDER
        .resolve("start_page.txt")
        .toFile()
        .readText()
        .toInt()

val fullTemplate get() = Report(START_PAGE)


fun main() {
    fullTemplate.save()
}