package report.graphviz.iteration

import algorithm.CPF
import algorithm.RESOURCES_FOLDER
import guru.nidi.graphviz.engine.Format
import guru.nidi.graphviz.engine.Graphviz
import report.OperatorNamer
import report.toPlainString


class CPFItreationsGraphSaver(val iterations: List<CPF.Iteration>) {
    private val programIndexes = ReducedProgram()
    private val namer = OperatorNamer(iterations)

    fun save() {
        parse().forEachIndexed { i, it ->
            Graphviz.fromString(it)
                    .render(Format.PNG)
                    .toFile(RESOURCES_FOLDER
                            .resolve("iterations")
                            .toAbsolutePath()
                            .resolve("${i + 1}.png")
                            .toFile())
        }
    }

    private fun parse() = iterations.map { parse(it) }

    private fun parse(cpfIteration: CPF.Iteration): String {
        programIndexes.iteration = cpfIteration

        return programIndexes.parse().run {
            fun Int.toName() = namer.name(cpfIteration.program, this).toPlainString()
            fun IntRange.toOperator() = map { SingleOperator(it.toName()) }

            toGraph(IntRange::toOperator)
        }
    }
}

