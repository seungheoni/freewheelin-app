package com.freewheelin.dto

import com.freewheelin.model.ProblemType
import java.time.LocalDateTime
import jakarta.validation.Valid
import jakarta.validation.constraints.*

data class ScoreRequest(
    @field:NotEmpty
    @field:Valid
    val answers: List<AnswerSubmission>
)

data class AnswerSubmission(
    @field:NotNull @field:Positive val problemId: Long,
    @field:NotNull
    val problemType: ProblemType,
    val studentAnswer: String
)

data class ScoreResponse(
    val pieceId: Long,
    val studentUserId: Long,
    val problemId: Long,
    val studentAnswer: String?,
    val correct: Boolean?,
    val scoredAt: LocalDateTime
)

data class ScoreListResponse(
    val scores: List<ScoreResponse>
)
