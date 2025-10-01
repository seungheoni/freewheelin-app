package com.freewheelin.dto
import java.time.LocalDateTime

data class PieceAnalysisResponse(
    val pieceId: Long,
    val pieceName: String,
    val assignedProblemCount: Int,
    val assignedStudents: List<StudentAnalysis>,
    val problemAnalysis: List<ProblemAnalysis>
)

data class StudentAnalysis(
    val studentUserId: Long,
    val studentName: String,
    val correctRate: Double,
    val answeredCount: Int,
    val correctCount: Int
)

data class ProblemAnalysis(
    val problemId: Long,
    val unitCode: String,
    val level: Int,
    val correctRate: Double,
    val totalAttempts: Int,
    val correctCount: Int
)
