package report.cpf.iteration

import algorithm.CPF
import algorithm.IOperator
import algorithm.RESOURCES_FOLDER
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import report.OperatorNamer
import report.toPlainString


class CPFItreationsGraphSaver(val iterations: List<CPF.Iteration>) {
    private val programIndexes = ReducedProgram()
    private val namer = OperatorNamer(iterations)

    fun saveAll() {
        savePlain()
        saveAsImage()
    }

    fun savePlain() {
        parse().forEachIndexed { i, it ->
            RESOURCES_FOLDER
                    .resolve("assets")
                    .resolve("cpf")
                    .resolve("plain")
                    .resolve("iterations")
                    .toAbsolutePath()
                    .resolve("${i + 1}.txt")
                    .toFile()
                    .apply {
                        createNewFile()
                        writeText(it)
                    }
        }
    }

    fun saveAsImage() {
        parse().forEachIndexed { i, it ->
            Graphviz.fromString(it)
                    .render(Format.PNG)
                    .toFile(RESOURCES_FOLDER
                            .resolve("assets")
                            .resolve("cpf")
                            .resolve("iterations")
                            .toAbsolutePath()
                            .resolve("${i + 1}")
                            .toFile())
        }
    }


    private fun parse() = iterations.zipWithNext().map { parse(it.first, it.second.program[it.first.groupedOperators.first]) }

    private fun parse(cpfIteration: CPF.Iteration, groupOperator: IOperator): String {
        programIndexes.iteration = cpfIteration

        return programIndexes.parse().run {
            fun Int.toName() = namer.name(cpfIteration.program, this).toPlainString()
            fun IntRange.toOperator() = map { SingleOperator(it.toName()) }

            toGraph(cpfIteration.number, namer.name(groupOperator).toPlainString(), IntRange::toOperator)
        }
    }
}

