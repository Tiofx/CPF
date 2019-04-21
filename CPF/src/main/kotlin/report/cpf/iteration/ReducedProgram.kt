package report.cpf.iteration

import algorithm.CPF
import kotlin.math.min

class ReducedProgram(val limit: Int = 17,
                     val groupMinSize: Int = 3,
                     val startMinSize: Int = 3,
                     val endMinSize: Int = 6) {
    lateinit var iteration: CPF.Iteration

    private val group get() = iteration.groupedOperators
    private val program get() = iteration.program

    val startMaxSize get() = group.start
    val endMaxSize get() = program.lastIndex - group.endInclusive
    val groupMaxSize get() = group.toList().size

    fun size(): TempSize = TempSize(startMinSize, groupMinSize, endMinSize)
            .minimaze()
            .maxGroupSize()
            .maxStartSize()
            .maxEndSize()

    val groupSize get() = size().group
    val startSize get() = size().start
    val endSize get() = size().end

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

    inner class TempSize(val start: Int, val group: Int, val end: Int) {
        val total get() = start + group + end
        val rem get() = limit - total

        fun minimaze(): TempSize = TempSize(
                min(startMinSize, startMaxSize),
                min(groupMinSize, groupMaxSize),
                min(endMinSize, endMaxSize)
        )

        fun maxGroupSize(): TempSize =
                if (groupMaxSize <= group + rem)
                    copy(group = groupMaxSize)
                else
                    copy(group = group + rem)

        fun maxStartSize(): TempSize =
                if (startMaxSize <= start + rem)
                    copy(start = startMaxSize)
                else
                    copy(start = start + rem)

        fun maxEndSize(): TempSize =
                if (endMaxSize <= end + rem)
                    copy(end = endMaxSize)
                else
                    copy(end = end + rem)

        fun copy(
                start: Int = this.start,
                group: Int = this.group,
                end: Int = this.end
        ) = TempSize(start, group, end)
    }

}
