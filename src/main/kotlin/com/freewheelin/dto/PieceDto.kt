package com.freewheelin.dto

import com.freewheelin.entity.Piece
import com.freewheelin.entity.PieceProblem
import com.freewheelin.model.UnitCode
import java.time.LocalDateTime
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.Valid
import javax.validation.constraints.*

data class PieceCreateRequest(
    @field:NotBlank
    val name: String,
    @field:NotEmpty
    @field:Size(max = 50)
    @field:Valid
    val problems: List<SelectedProblemRequest>
)

data class SelectedProblemRequest(
    @field:NotNull
    @field:Positive
    val id: Long,
    @field:NotBlank
    @field:Pattern(regexp = "(?i)^uc\\d{4}$")
    val unitCode: String,
    @field:Min(1)
    @field:Max(5)
    val level: Int
)

data class PieceResponse(
    val id: Long,
    val name: String,
    val teacherUserId: Long,
    val teacherName: String,
    val problemCount: Int,
    val createdAt: LocalDateTime
)

data class PieceOrderUpdateRequest(
    @Schema(description = "변경된 순서의 problemId 전체 목록 (화면에 보이는 전체)", example = "[351, 210, 999]")
    @field:NotEmpty
    @field:Size(max = 50)
    val orderedProblemIds: List<@Positive Long>
)

data class PieceProblemResponse(
    val id: Long,
    val unitCode: String,
    val level: Int,
    val problemType: String,
    val orderIndex: Int
)

data class PieceProblemsResponse(
    val pieceId: Long,
    val pieceName: String,
    val problems: List<PieceProblemResponse>
)

data class StudentStat(
    val studentId: Long,
    val studentName: String,
    val correctCount: Long,
    val totalCount: Long
)

data class ProblemStat(
    val problemId: Long,
    val correctCount: Long,
    val totalCount: Long
)
