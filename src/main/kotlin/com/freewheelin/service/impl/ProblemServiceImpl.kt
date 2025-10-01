package com.freewheelin.service.impl

import com.freewheelin.dto.ProblemListResponse
import com.freewheelin.dto.ProblemResponse
import com.freewheelin.dto.ProblemSearchRequest
import com.freewheelin.dto.toResponse
import com.freewheelin.entity.Problem as ProblemEntity
import com.freewheelin.model.ProblemType
import com.freewheelin.repository.ProblemRepository
import com.freewheelin.service.ProblemService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.math.floor
import kotlin.random.Random

@Service
class ProblemServiceImpl(
    private val problemRepository: ProblemRepository
) : ProblemService {

    @Transactional(readOnly = true)
    override fun searchProblems(request: ProblemSearchRequest): ProblemListResponse {
        val requestedProblemTypes: List<ProblemType> = when (request.problemType) {
            ProblemType.ALL -> listOf(ProblemType.SUBJECTIVE, ProblemType.SELECTION)
            else -> listOf(request.problemType)
        }

        val totalCount: Int = request.totalCount
        val levelRates: List<Double> = request.level.rates()
        val desiredCountsByLevel: List<Double> = levelRates.map { it * totalCount }
        val computedCountsByLevel: MutableList<Int> = desiredCountsByLevel.map { floor(it).toInt() }.toMutableList()
        val remainingCount: Int = totalCount - computedCountsByLevel.sum()
        if (remainingCount > 0) {
            distributeRemainingCountsByLevel(computedCountsByLevel, desiredCountsByLevel, remainingCount)
        }

        val (lowLimit, midLimit, highLimit) = computedCountsByLevel

        val unitCodeStrings: List<String> = request.unitCodeList.map { it.value }

        val cutoff: LocalDateTime = LocalDateTime.now().minusYears(1)

        val problemLowLevelIds: List<Long> = problemRepository.findIdsByFilters(
            unitCodes = unitCodeStrings,
            problemTypes = requestedProblemTypes,
            levels = listOf(1),
            cutoff = cutoff
        )
        val problemMiddleLevelIds: List<Long> = problemRepository.findIdsByFilters(
            unitCodes = unitCodeStrings,
            problemTypes = requestedProblemTypes,
            levels = listOf(2, 3, 4),
            cutoff = cutoff
        )
        val problemHighLevelIds: List<Long> = problemRepository.findIdsByFilters(
            unitCodes = unitCodeStrings,
            problemTypes = requestedProblemTypes,
            levels = listOf(5),
            cutoff = cutoff
        )

        val candidateProblemIds: List<Long> = buildList {
            addAll(pickRandomIds(problemLowLevelIds, lowLimit))
            addAll(pickRandomIds(problemMiddleLevelIds, midLimit))
            addAll(pickRandomIds(problemHighLevelIds, highLimit))
        }.shuffled(Random(System.nanoTime()))

        val problemsInScope: List<ProblemEntity> =
            if (candidateProblemIds.isEmpty()) emptyList()
            else problemRepository.findAllById(candidateProblemIds).toList()

        val selectedProblems: MutableList<ProblemEntity> = problemsInScope.take(totalCount).toMutableList()

        val remaining: Int = totalCount - selectedProblems.size
        if (remaining > 0) {
            appendRemainingProblemsFromIds(
                remaining = remaining,
                allBaseIds = problemLowLevelIds + problemMiddleLevelIds + problemHighLevelIds,
                alreadyPickedIds = candidateProblemIds,
                selectedProblems = selectedProblems
            )
        }

        val finalList: List<ProblemResponse> = selectedProblems
            .shuffled()
            .take(totalCount)
            .map { it.toResponse() }
        return ProblemListResponse(finalList)
    }

    fun pickRandomIds(ids: List<Long>, limit: Int): List<Long> {
        if (limit <= 0 || ids.isEmpty()) return emptyList()
        if (ids.size <= limit) return ids.shuffled(Random(System.nanoTime()))
        val result = ArrayList<Long>(limit)
        val picked = HashSet<Int>(limit * 2)
        val rand = Random(System.nanoTime())
        while (result.size < limit) {
            val idx = rand.nextInt(ids.size)
            if (picked.add(idx)) result.add(ids[idx])
        }
        return result
    }

    private fun distributeRemainingCountsByLevel(
        computedCountsByLevel: MutableList<Int>,
        desiredCountsByLevel: List<Double>,
        remainingCount: Int
    ) {
        if (remainingCount <= 0) return
        val extraAllocationOrderByLevel: List<Int> = desiredCountsByLevel
            .mapIndexed { index, desired -> index to (desired - floor(desired)) }
            .sortedByDescending { it.second }
            .map { it.first }
        var extraAllocationIndex: Int = 0
        repeat(remainingCount) {
            val bucketIndex: Int = extraAllocationOrderByLevel[extraAllocationIndex]
            computedCountsByLevel[bucketIndex] = computedCountsByLevel[bucketIndex] + 1
            extraAllocationIndex = (extraAllocationIndex + 1) % extraAllocationOrderByLevel.size
        }
    }

    private fun appendRemainingProblemsFromIds(
        remaining: Int,
        allBaseIds: Collection<Long>,
        alreadyPickedIds: Collection<Long>,
        selectedProblems: MutableList<ProblemEntity>
    ) {
        if (remaining <= 0 || allBaseIds.isEmpty()) return
        val alreadySelectedIds: Set<Long> = selectedProblems.mapNotNull { it.id }.toSet()
        val availableExtraIds: List<Long> = allBaseIds.asSequence()
            .filter { it !in alreadyPickedIds }
            .filter { it !in alreadySelectedIds }
            .toList()
        val extraIds: List<Long> = pickRandomIds(availableExtraIds, remaining * 2)
        if (extraIds.isEmpty()) return
        val extraProblems: List<ProblemEntity> = problemRepository
            .findAllById(extraIds)
            .toList()
            .take(remaining)
        selectedProblems.addAll(extraProblems)
    }
}


