package com.freewheelin.service.impl

import com.freewheelin.dto.*
import com.freewheelin.entity.*
import com.freewheelin.repository.*
import com.freewheelin.service.PieceService
import com.freewheelin.exception.ErrorMessage
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PieceServiceImpl(
    private val pieceRepository: PieceRepository,
    private val pieceProblemRepository: PieceProblemRepository,
    private val pieceStudentRepository: PieceStudentRepository,
    private val answerRepository: AnswerRepository,
    private val problemRepository: ProblemRepository,
    private val userRepository: UserRepository
) : PieceService {
    companion object {
        private const val ORDER_STEP: Int = 1000
    }
    
    
    @Transactional
    override fun createPiece(teacherUserId: Long, request: PieceCreateRequest): PieceResponse {
        val teacher: User = userRepository.findById(teacherUserId)
            .orElseThrow { IllegalArgumentException(ErrorMessage.TEACHER_NOT_FOUND.format(teacherUserId)) }

        if (teacher.role != UserRole.TEACHER) {
            throw IllegalArgumentException(ErrorMessage.TEACHER_ONLY_CREATE.defaultMessage)
        }

        if (request.problems.size > 50) {
            throw IllegalArgumentException(ErrorMessage.PIECE_MAX_PROBLEMS_EXCEEDED.defaultMessage)
        }

        val piece: Piece = Piece(
            name = request.name,
            creator = teacher
        )
        val savedPiece: Piece = pieceRepository.save(piece)

        val requestedOrder: List<SelectedProblemRequest> = request.problems
            .sortedWith(compareBy({ it.unitCode }, { it.level }))
        val problemsById: Map<Long, Problem> = problemRepository.findAllById(request.problems.map { it.id })
            .associateBy { it.id!! }

        val requestedIds: Set<Long> = request.problems.map { it.id }.toSet()
        val foundIds: Set<Long> = problemsById.keys.toSet()
        val missingIds: Set<Long> = requestedIds - foundIds
        if (missingIds.isNotEmpty()) {
            throw IllegalArgumentException(ErrorMessage.PROBLEM_IDS_MISSING.format(missingIds))
        }

        val orderedProblems: List<Problem> = requestedOrder.map { problemsById[it.id]!! }
        val STEP: Int = 1000
        val pieceProblemsToSave: List<PieceProblem> = orderedProblems.mapIndexed { index: Int, problem: Problem ->
            PieceProblem(
                piece = savedPiece,
                problem = problem,
                orderNo = (index + 1) * STEP
            )
        }
        pieceProblemRepository.saveAll(pieceProblemsToSave)

        return savedPiece.toResponse(orderedProblems.size)
    }

    @Transactional
    override fun updatePieceOrder(pieceId: Long, teacherUserId: Long, request: PieceOrderUpdateRequest) {
        val teacher = userRepository.findById(teacherUserId)
            .orElseThrow { IllegalArgumentException(ErrorMessage.TEACHER_NOT_FOUND.format(teacherUserId)) }

        if (teacher.role != UserRole.TEACHER) {
            throw IllegalArgumentException(ErrorMessage.TEACHER_ONLY_UPDATE_ORDER.defaultMessage)
        }

        val piece : Piece = pieceRepository.findByCreatorAndId(teacher, pieceId)
            ?: throw IllegalArgumentException(ErrorMessage.PIECE_NOT_FOUND.format(pieceId))

        val pieceProblems: List<PieceProblem> = pieceProblemRepository.findByPieceWithProblemOrderByOrderNo(piece)
        val currentIds: List<Long> = pieceProblems.map { it.problem.id!! }
        val requestedIds: List<Long> = request.orderedProblemIds

        val currentSet: Set<Long> = currentIds.toSet()
        val requestedSet: Set<Long> = requestedIds.toSet()
        val hasDuplicates: Boolean = requestedIds.size != requestedSet.size
        if (hasDuplicates || requestedSet != currentSet) {
            throw IllegalArgumentException(ErrorMessage.PROBLEM_IDS_SET_MISMATCH.defaultMessage)
        }

        val pieceProblemById: Map<Long, PieceProblem> = pieceProblems.associateBy { it.problem.id!! }
        requestedIds.forEachIndexed { index: Int, problemId: Long ->
            pieceProblemById.getValue(problemId).orderNo = (index + 1) * ORDER_STEP
        }
    }

    @Transactional(readOnly = true)
    override fun getPieceProblems(pieceId: Long, studentUserId: Long): PieceProblemsResponse {
        val piece: Piece = pieceRepository.findById(pieceId)
            .orElseThrow { IllegalArgumentException(ErrorMessage.PIECE_NOT_FOUND.format(pieceId)) }

        val student: User = userRepository.findById(studentUserId)
            .orElseThrow { IllegalArgumentException(ErrorMessage.STUDENT_NOT_FOUND.format(studentUserId)) }
        pieceStudentRepository.findByPieceAndStudent(piece, student)
            ?: throw IllegalArgumentException(ErrorMessage.PIECE_NOT_ASSIGNED_TO_USER.defaultMessage)

        val pieceProblems: List<PieceProblem> = pieceProblemRepository.findByPieceWithProblemOrderByOrderNo(piece)
        val problems: List<PieceProblemResponse> = pieceProblems.map { pieceProblem: PieceProblem ->
            PieceProblemResponse(
                id = pieceProblem.problem.id!!,
                unitCode = pieceProblem.problem.unitCode.code,
                level = pieceProblem.problem.level,
                problemType = pieceProblem.problem.problemType.name,
                orderIndex = pieceProblem.orderNo ?: 0
            )
        }

        return PieceProblemsResponse(
            pieceId = piece.id!!,
            pieceName = piece.name,
            problems = problems
        )
    }

    @Transactional(readOnly = true)
    override fun analyzePiece(pieceId: Long, teacherUserId: Long): PieceAnalysisResponse {
        val teacher = userRepository.findById(teacherUserId)
            .orElseThrow { IllegalArgumentException(ErrorMessage.TEACHER_NOT_FOUND.format(teacherUserId)) }

        if (teacher.role != UserRole.TEACHER) {
            throw IllegalArgumentException(ErrorMessage.TEACHER_ONLY_CREATE.defaultMessage)
        }

        val piece = pieceRepository.findByCreatorAndId(teacher, pieceId)
            ?: throw IllegalArgumentException(ErrorMessage.PIECE_NOT_FOUND.format(pieceId))

        val studentStats = answerRepository.findStudentStatsByPiece(piece)
        val studentAnalyses = studentStats.map { stat ->
            StudentAnalysis(
                studentUserId = stat.studentId,
                studentName = stat.studentName,
                correctRate = if (stat.totalCount > 0) stat.correctCount.toDouble() / stat.totalCount else 0.0,
                answeredCount = stat.totalCount.toInt(),
                correctCount = stat.correctCount.toInt()
            )
        }

        val pieceProblems = pieceProblemRepository.findByPieceWithProblemOrderByOrderNo(piece)
        val problemStats = answerRepository.findProblemStatsByPiece(piece).associateBy { it.problemId }
        val problemAnalyses = pieceProblems.map { pp ->
            val stat = problemStats[pp.problem.id]
            val correctCount = stat?.correctCount ?: 0L
            val totalAttempts = stat?.totalCount ?: 0L
            ProblemAnalysis(
                problemId = pp.problem.id!!,
                unitCode = pp.problem.unitCode.code,
                level = pp.problem.level,
                correctRate = if (totalAttempts > 0) correctCount.toDouble() / totalAttempts else 0.0,
                totalAttempts = totalAttempts.toInt(),
                correctCount = correctCount.toInt()
            )
        }

        return PieceAnalysisResponse(
            pieceId = piece.id!!,
            pieceName = piece.name,
            assignedProblemCount = pieceProblems.size,
            assignedStudents = studentAnalyses,
            problemAnalysis = problemAnalyses
        )
    }
}

private fun Piece.toResponse(problemCount: Int): PieceResponse {
        return PieceResponse(
        id = this.id!!,
        name = this.name,
            teacherUserId = this.creator.id!!,
        teacherName = this.creator.name,
        problemCount = problemCount,
        createdAt = LocalDateTime.now()
    )
}

 
