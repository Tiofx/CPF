package buildtask.report

import report.ProgramCodeTemplate
import java.nio.file.Paths

private val PATH_TO_PROGRAM = Paths.get("")
        .resolve("src")
        .resolve("main")
        .resolve("kotlin")
        .resolve("program.kt")
        .toFile()

private val codeTemplate = ProgramCodeTemplate(PATH_TO_PROGRAM)

fun main() {
    codeTemplate.save()
}