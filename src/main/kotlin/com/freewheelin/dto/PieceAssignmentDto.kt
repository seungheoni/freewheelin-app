package com.freewheelin.dto

import java.time.LocalDateTime
import javax.validation.constraints.NotEmpty
import javax.validation.constraints.NotNull
import javax.validation.constraints.Positive

data class PieceAssignRequest(
    @field:NotEmpty
    val studentUserIds: List<@NotNull @Positive Long>
)

data class PieceAssignResponse(
    val pieceId: Long,
    val pieceName: String,
    val studentUserId: Long,
    val studentName: String,
    val assignedAt: LocalDateTime
)
