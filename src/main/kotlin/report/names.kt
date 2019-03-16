package report

import algorithm.CPF
import algorithm.IOperator
import algorithm.Program
import algorithm.indices

class OperatorNamer(val iterations: List<CPF.Iteration>) {
    private val names: Map<IOperator, OperatorName> by lazy {
        var parallelIndex = 1
        var sequentialIndex = 1
        val result = mutableMapOf<IOperator, OperatorName>()

        iterations.first().program.forEachIndexed { i, op ->
            result[op] = OriginOperator(i + 1)
        }

        iterations.dropLast(1).forEachIndexed { i, it ->
            if (it.isParallel) {
                val opInd = it.parallelCheck!!.maxChain!!.first
                val op = iterations[i + 1].program[opInd]
                result[op] = GroupParallelOperator(parallelIndex)
                parallelIndex++
            } else {
                val opInd = it.sequentialCheck!!.first
                val op = iterations[i + 1].program[opInd]
                result[op] = GroupSequentialOperator(sequentialIndex)
                sequentialIndex++
            }
        }

        result
    }

    fun name(operator: IOperator) = names[operator] ?: UnknownName
    fun name(program: Program, i: Int) = names[program.S(i)] ?: UnknownName
    fun names(program: Program) = program.indices.map { name(program, it) }
    fun names() = iterations.map(CPF.Iteration::program).map { names(it) }
}


sealed class OperatorName(val i: Int)
object UnknownName : OperatorName(-1)
class OriginOperator(i: Int) : OperatorName(i)
class GroupParallelOperator(i: Int) : OperatorName(i)
class GroupSequentialOperator(i: Int) : OperatorName(i)

