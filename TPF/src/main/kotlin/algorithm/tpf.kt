package algorithm

import report.toLatex
import kotlin.system.measureTimeMillis

fun main() {
    println("======")
//    println(allCombinations(5,3))
//    println(allCombinations(5,3).map { it.asReversed() }
//        .sortedBy {
//            it.map { it.toDouble() }
//                .reduceIndexed { index, i, acc -> acc + i * Math.pow(10.0, index.toDouble()) }
//        })
//    return

    measureTimeMillis {
        CashedTPF(Program.from()).form(8).forEachIndexed { i, iteration ->
            //            if (i > 5) return@measureTimeMillis

            iteration.arityCondition
                .filter { it.fullCase.isNotEmpty() }
                .onEach {
                    println(it.toLatex().toShortLatex())
                    println("\\\\")
                }

//            iteration.arityCondition
//                .map { it.fullCase }
//                .filter { it.isNotEmpty() }
//                .onEach {
//                    it.filter { it.unionOf.isNotEmpty() }
//                        .onEach {
//                        println(it.toLatex().toLatex())
//                        println("\\\\")
//                    }
//                    println("\\\\")
//                }

//            iteration.arityCondition.map { it.fullCase.map { it.unionOf } }
//                .onEach {
//                    it.onEach {
//                        it.onEach {
//                            println(it.toLatex().toLatex())
//                            println("\\\\")
//                        }
//                        println("\\\\")
//                    }
//                    println("\\\\")
//                }


//            iteration.arityCondition.map { it.fullCase.map { it.unionOf.map { it.intersect } } }
//                .onEach {
//                    it.onEach {
//                        it.onEach {
//                            it.onEach {
//                                println(it.toLatex().stepSequence.joinToString(" = "))
//                            }
//                            println("\\\\")
//                        }
//                        println("\\\\")
//                    }
//                    println("\\\\")
//                }
        }
//        CashedTPF(Program.from()).form(8).forEach { println(it.map { it.j }) }
    }.let { println(it / 1000.0) }
    println("======")
}

class CashedTPF(program: Program) : TPF(program) {
    private val operationForInitNeedCache: MutableMap<Pair<Int, Int>, Set<Int>> = mutableMapOf()
//    private val singleCaseCache: MutableMap<Int, Set<Int>> = mutableMapOf()
//
//
//    override fun singleCase(formedTiers: List<Set<algorithm.Operation>>, arity: List<Int>, tier: List<Int>): Set<Int> {
//        if (!singleCaseCache.containsKey((arity to tier).hashCode())) {
//            val result = super.singleCase(formedTiers, arity, tier)
//            singleCaseCache[(arity to tier).hashCode()] = result
//        }
//
//        return singleCaseCache[(arity to tier).hashCode()]!!
//    }

    override fun operationsForInitNeed(
        formedTiers: List<Set<Operation>>,
        fromTier: Int,
        operationNumber: Int
    ): Set<Int> {
        if (!operationForInitNeedCache.containsKey(fromTier to operationNumber)) {
            val result = super.operationsForInitNeed(formedTiers, fromTier, operationNumber)
            operationForInitNeedCache[fromTier to operationNumber] = result
        }

        return this@CashedTPF.operationForInitNeedCache[fromTier to operationNumber]!!
    }

}

open class TPF(val program: Program) {
    private val J = program.operators.indices
    private val List<Set<Operation>>.used get() = flatten().map(Operation::j)
    private val List<Set<Operation>>.unUsed get() = J - used
    private val List<Set<Operation>>.unusedGroupedByArity get() = unUsed.groupBy { program.operators[it].type.n }
    private val List<Set<Operation>>.maxRemArity get() = unUsed.map { program.operators[it].type.n }.max()!!

    fun form(zeroTierSize: Int): List<Iteration> {
        val formedTiers = mutableListOf<Set<Operation>>()
        val processModels = mutableListOf<ProcessModel>()
        val tierW = mutableListOf<List<Set<Int>>>()
        val arityConditions = mutableListOf<List<ArityCondition>>()
        val listOfGrouptedByArity = mutableListOf<Map<Int, List<Int>>>()

//        formedTiers += formedTiers.unUsed.map { program.operators[it] }.filter { it.input.isEmpty() }.toSet()
        val unUsed = formedTiers.unUsed
        formedTiers += program.operators.subList(0, zeroTierSize).toSet()
        processModels += ProcessModel(
            unUsed.toSet(),
            formedTiers.used.toSet(),
            worked = formedTiers.used.toSet()
//            switchOff = formedTiers.used.toSet()
        )
        tierW += formedTiers.last().map { it.w }
        arityConditions += emptyList<ArityCondition>()
        listOfGrouptedByArity += emptyMap()
        var formedTierNumber = 1

//        println(processModels.last())
        while (formedTiers.unUsed.isNotEmpty()) {
            val notReady = processModels.last().run { notReady subtract ready }
//            println(processModels.flatMap { it.worked }.toSet().map { program.operators[it].output })
            val ready =
                notReady.filter {
                    program.operators[it].input.toSet().run {
                        this.isEmpty() || this in processModels.flatMap { it.worked }.toSet().flatMap { program.operators[it].output }.toSet()
                    }
                }
                    .toSet()
            val switchOn = ready
            val worked = (processModels.last().worked union switchOn) subtract processModels.last().switchOff
            val switchOff = processModels.last().switchOn
            processModels += ProcessModel(notReady, ready, switchOn, worked, switchOff)

            val groupByArity = processModels.last().switchOn.groupBy { program.operators[it].type.n }
            listOfGrouptedByArity += groupByArity

            arityConditions += groupByArity
                .map { formedTiers.arityConditionData(it.key, formedTierNumber) }

//            if (formedTierNumber < 12)
//                arityConditions.last()
//                    .onEach {
//                        println(it)
//                        println()
//                    }
//            println("=============")
//            val fullCaseForEachArity = formedTiers.fullCaseForEachArity(formedTiers.maxRemArity, formedTierNumber)

            val nextTier =
                formedTiers.unusedGroupedByArity
                    .flatMap { it.value.toSet() intersect formedTiers.fullCaseForArity(it.key, formedTierNumber) }
                    .map { program.operators[it] }
                    .toSet()

            formedTiers += nextTier
            tierW += formedTiers.last().map { it.w }

            println(processModels.last().worked)
//            println(formedTierNumber)
//            fullCaseForEachArity.forEach { println(it) }
//            formedTiers.unusedGroupedByArity.let { println(it) }
//            println(nextTier.map { it.j })
//            println()

            formedTierNumber++
        }


        return formedTiers.indices.map {
            Iteration(
                processModels[it],
                arityConditions[it],
                listOfGrouptedByArity[it],
                formedTiers[it].map { it.j }.toSet(),
                tierW[it]
            )
        }
    }

    data class Iteration(
        val processModel: ProcessModel,
        val arityCondition: List<ArityCondition>,
        val operationByArity: Map<Int, List<Int>>,
        val tier: Set<Int>,
        val tierW: List<Set<Int>>
    )

    data class ArityCondition(
        val arity: Int,
        val tierK: Int,
        val fullCase: List<SingleCase>,
        val result: Set<Int> = fullCase
            .map { it.result }
            .fold(emptySet<Int>()) { acc, e -> acc union e }
            .sorted()
            .toSet()
    ) {

        data class SingleCase(
            val unionOf: List<SingleCasePart>,
            val result: Set<Int> = unionOf
                .map { it.result }
                .fold(emptySet<Int>()) { acc, e -> acc union e }
                .sorted()
                .toSet()
        )

        data class SingleCasePart(
//            val tierK: Int,
            val intersect: List<SubCase>,
            val result: Set<Int> = intersect
                .map { it.result }
                .reduce { acc, e -> acc intersect e }
                .sorted()
                .toSet()
        )

        data class SubCase(
            val arity: Int,
            val tier: Int,
            val union: List<SubCasePart>,
            val result: Set<Int> = union
                .map { it.result }
                .fold(emptySet<Int>()) { acc, e -> acc union e }
                .sorted()
                .toSet()
        )

        data class SubCasePart(
            val intersect: List<Element>,
            val result: Set<Int> = intersect
                .map { it.w }
                .reduce { acc, e -> acc intersect e }
                .sorted()
                .toSet()
        )

        data class Element(
            val i: Int,
            val w: Set<Int>
        )
    }

    private fun List<Set<Operation>>.arityConditionData(a: Int, currentTier: Int): ArityCondition {
        val aritys = allCombinationOfArity(a)
            .filter { aritySet ->
                val minDifferentTierNumber =
                    aritySet.size - aritySet.indexOf(0).let { if (it == -1) 0 else aritySet.size - it }
                minDifferentTierNumber <= size
            }
            .map { it.take(currentTier) }

        return arityConditionData(aritys, currentTier)
    }

    protected fun List<Set<Operation>>.arityConditionData(aritys: AritySetCombination, currentTier: Int) =
        aritys.map { a ->
            singleCaseData(a, tierNumbers(currentTier, a.minLayerNumber))
        }
            .let { ArityCondition(aritys.first().sum(), currentTier, it) }


    protected fun List<Set<Operation>>.singleCaseData(
        arity: List<Int>,
        tiers: TierSetCombinations
    ) =
        tiers
            .filter { arityNotExceedTierSize(it, arity) }
            .map { tier -> singleCasePartData(this, arity, tier) }
            .let { ArityCondition.SingleCase(it) }

    protected fun singleCasePartData(
        formedTiers: List<Set<Operation>>,
        arity: List<Int>,
        tier: List<Int>
    ) = tier.indices
        .map { j ->
            ArityCondition.SubCase(arity[j], tier[j], subCaseParts(formedTiers, tier[j], arity[j]))
        }.let { ArityCondition.SingleCasePart(it) }

    protected open fun subCaseParts(
        formedTiers: kotlin.collections.List<kotlin.collections.Set<Operation>>,
        fromTier: kotlin.Int,
        operationNumber: kotlin.Int
    ): List<ArityCondition.SubCasePart> {
        fun List<Set<Int>>.subCasePartList(intersectNumber: Int) =
            allCombinations(this@subCasePartList.lastIndex, intersectNumber)
                .map { it.asReversed() }
                .sortedBy {
                    it.map { it.toDouble() }
                        .reduceIndexed { index, i, acc -> acc + i * Math.pow(10.0, index.toDouble()) }
                }.map { it.map { ArityCondition.Element(it, this[it]) } }
                .map { ArityCondition.SubCasePart(it) }


        require(fromTier <= formedTiers.lastIndex) { "fromTier exceed number of tiers" }
        if (operationNumber > formedTiers.used.size) return listOf(ArityCondition.SubCasePart(emptyList(), emptySet()))

        return when (operationNumber) {
            0 -> formedTiers.unUsed.let { ArityCondition.SubCasePart(emptyList(), it.toSet()) }.let { listOf(it) }
            else -> formedTiers[fromTier].map(Operation::w).subCasePartList(operationNumber)
        }
    }

    data class ProcessModel(
        val notReady: Set<Int>,
        val ready: Set<Int>,
        val switchOn: Set<Int> = ready,
        val worked: Set<Int> = emptySet(),
        val switchOff: Set<Int> = emptySet()
    )

//    inline private fun List<Set<Operation>>.fullCaseForEachArity(maxArity: Int, currentTier: Int) =
//        (0..maxArity).map { aEstimate ->
//            aEstimate to fullCaseForArity(aEstimate, currentTier)
//        }

    inline private fun List<Set<Operation>>.fullCaseForArity(a: Int, currentTier: Int): Set<Int> {
        val aritys = allCombinationOfArity(a)
            .filter { aritySet ->
                val minDifferentTierNumber =
                    aritySet.size - aritySet.indexOf(0).let { if (it == -1) 0 else aritySet.size - it }
                minDifferentTierNumber <= size
            }
            .map { it.take(currentTier) }

        return fullCase(aritys, currentTier)
    }

    inline private fun List<Set<Operation>>.fullCase(aritys: AritySetCombination, currentTier: Int) =
        aritys.flatMap { a ->
            singleCase(a, tierNumbers(currentTier, a.minLayerNumber))
//                .map { tier ->
//                    if (tier.indices.none { j -> a[j] > this[tier[j]].size })
//                        singleCasePart(this, a, tier)
//                    else
//                        emptySet()
//                }
        }
            .fold(emptySet<Int>()) { acc, set -> acc union set }


    inline private fun List<Set<Operation>>.singleCase(
        arity: List<Int>,
        tiers: TierSetCombinations
    ) =
        tiers
            .filter { arityNotExceedTierSize(it, arity) }
            .map { tier -> singleCasePart(this, arity, tier) }

    inline private fun List<Set<Operation>>.arityNotExceedTierSize(tier: List<Int>, arity: List<Int>) =
        tier.indices.none { j -> arity[j] > this[tier[j]].size }

    inline private fun singleCasePart(
        formedTiers: List<Set<Operation>>,
        arity: List<Int>,
        tier: List<Int>
    ) =
        singleCasePartSets(formedTiers, arity, tier).reduce { acc, set -> acc intersect set }


    private fun singleCasePartSets(
        formedTiers: List<Set<Operation>>,
        arity: List<Int>,
        tier: List<Int>
    ) =
        tier.indices
            .map { j -> operationsForInitNeed(formedTiers, tier[j], arity[j]) }

    protected open fun operationsForInitNeed(
        formedTiers: kotlin.collections.List<kotlin.collections.Set<Operation>>,
        fromTier: kotlin.Int,
        operationNumber: kotlin.Int
    ): Set<Int> {
        return operationsForInitNeedSets(formedTiers, fromTier, operationNumber).reduce { acc, set -> acc union set }
//        fun List<Set<Int>>.unionOfIntersect(intersectNumber: Int) =
//            allCombinations(this@unionOfIntersect.lastIndex, intersectNumber)
//                .map { it.map { this[it] } }
//                .map { it.reduce { acc, set -> acc intersect set } }
//                .reduce { acc, set -> acc union set }
//
//        require(fromTier <= formedTiers.lastIndex) { "fromTier exceed number of tiers" }
//        if (operationNumber > formedTiers.used.size) return emptySet()
//
//        return when (operationNumber) {
//            0 -> formedTiers.unUsed.toSet()
//            else -> formedTiers[fromTier].map(Operation::w).unionOfIntersect(operationNumber)
//        }

//        return when (a) {
//            0 -> unUsed.toSet()
//            1 -> this[fromTier].map(algorithm.Operation::w).fold(emptySet<Int>()) { acc, set -> acc union set }.sorted().toSet()
//            else -> this[fromTier].map(algorithm.Operation::w).unionOfIntersect(a).toList().sorted().toSet()
//        }
    }

    protected open fun operationsForInitNeedSets(
        formedTiers: kotlin.collections.List<kotlin.collections.Set<Operation>>,
        fromTier: kotlin.Int,
        operationNumber: kotlin.Int
    ): List<Set<Int>> {
        fun List<Set<Int>>.unionOfIntersect(intersectNumber: Int) =
            allCombinations(this@unionOfIntersect.lastIndex, intersectNumber)
                .map { it.map { this[it] } }
                .map { it.reduce { acc, set -> acc intersect set } }

        require(fromTier <= formedTiers.lastIndex) { "fromTier exceed number of tiers" }
        if (operationNumber > formedTiers.used.size) return listOf(emptySet())

        return when (operationNumber) {
            0 -> formedTiers.unUsed.toSet().let { listOf(it) }
            else -> formedTiers[fromTier].map(Operation::w).unionOfIntersect(operationNumber)
        }
    }

}

typealias TierSetCombinations = List<List<Int>>
typealias AritySet = List<Int>
typealias AritySetCombination = List<AritySet>

val AritySet.minLayerNumber get() = indexOf(0).let { if (it == -1) size else it }

fun TierSetCombinations.distinctBy(a: AritySet) = distinctBy {
    val dropNumber = a.size - (a.indexOf(0).let { if (it == -1) a.size else it })
    it.dropLast(dropNumber)
}

fun allCombinationOfArity(a: Int): AritySetCombination =
    if (a != 0) (0 until Math.pow(2.0, a.toDouble() - 1).toInt()).map { arityOnTiers(a, it) }
    else listOf(listOf(0))

fun arityOnTiers(a: Int, i: Int): AritySet =
    when (a) {
        0 -> emptyList()
        1 -> listOf(1)
        2 -> listOf(a - i, i)

        else -> {
            val iPow2 = Math.ceil(Math.log(i + 1.0) / Math.log(2.0)).toInt()

            val first = a - iPow2
            val middle = arityOnTiers(iPow2, i - Math.pow(2.0, iPow2 - 1.0).toInt())
            val end = List(a - (iPow2 + 1)) { 0 }

            listOf(first) + middle + end
        }
    }

fun tierNumbers(k: Int, a: Int): TierSetCombinations {
//    if (a > k) return emptyList()
    if (a == 0) return listOf(listOf(k - 1))
//    if (a == 0) return emptyList()
    if (a == 1) return listOf(listOf(k - 1))
    if (a > k) return listOf((k - 1 downTo 0).toList())

    return allCombinations(k - 2, a - 1).preapend(k - 1)
}


fun List<List<Int>>.preapend(i: Int) = map { listOf(i) + it }

fun allCombinations(max: Int, remNumber: Int): List<List<Int>> {
    if (remNumber == 1) return (max downTo 0).map { listOf(it) }

    return (max downTo 0).flatMap { i ->
        allCombinations(i - 1, remNumber - 1).preapend(i)
    }
}

operator fun <E> Set<E>.contains(s: Set<E>) = s.all { it in this }