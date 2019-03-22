package report.graphviz.iteration

import algorithm.CPF
import kotlin.math.min

class ReducedProgram(val limit: Int = 15,
//                     val groupMinSize: Int = 3,
                     val startMinSize: Int = 2,
                     val endMinSize: Int = 2) {
    lateinit var iteration: CPF.Iteration

    val group get() = iteration.groupedOperators
    val program get() = iteration.program

    val startMaxSize get() = group.start
    val endMaxSize get() = program.lastIndex - group.endInclusive
    val groupMaxSize get() = group.toList().size

    val remFromStart get() = remNumber(startMinSize, startMaxSize)
    val remFromEnd get() = remNumber(endMinSize, endMaxSize)

    val groupPreferredSize get() = limit - (startMinSize + endMinSize) + remFromStart + remFromEnd

    val groupSize get() = min(groupPreferredSize, groupMaxSize)
    val startSize get() = min((limit - groupSize) / 2 + (limit - groupSize) % 2, group.start)
    val endSize get() = min(limit - (startSize + groupSize), program.lastIndex - group.endInclusive)

    val startRange get() = if (startSize > 0) 0..(startSize - 1) else IntRange.EMPTY
    val groupSkipAdditionalSpace get() = if (groupMaxSize - groupSize == 1) 1 else 0
    val groupStartRange get() = group.start..(group.start + groupSize / 2 + groupSize % 2 - 1 - groupSkipAdditionalSpace)
    val groupEndRange get() = (group.endInclusive - groupSize / 2 + 1)..group.endInclusive
    val endRange get() = if (endSize > 0) (program.lastIndex - endSize + 1)..program.lastIndex else IntRange.EMPTY

    fun parse() = Indexes(
            startRange,
            groupStartRange,
            groupEndRange,
            endRange
    )

    data class Indexes(
            val start: IntRange,
            val groupStart: IntRange,
            val groupEnd: IntRange,
            val end: IntRange
    ) {
        val hasSkipBeforeGroup: Boolean = !start.isEmpty() && start.endInclusive + 1 != groupStart.start
        val hasSkipInGroup: Boolean = !groupStart.isEmpty() && groupStart.endInclusive + 1 != groupEnd.start
        val hasSkipBeforeEnd: Boolean = !end.isEmpty() && groupEnd.endInclusive + 1 != end.start
    }

    private fun remNumber(min: Int, max: Int) = if (max < min) min - max else 0
}