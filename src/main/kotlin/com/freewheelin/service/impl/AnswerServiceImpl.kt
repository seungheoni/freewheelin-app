package com.freewheelin.service.impl

import com.freewheelin.dto.ScoreListResponse
import com.freewheelin.dto.ScoreRequest
import com.freewheelin.dto.ScoreResponse
import com.freewheelin.entity.Answer
import com.freewheelin.model.ProblemType
import com.freewheelin.entity.UserRole
import com.freewheelin.exception.ErrorMessage
import com.freewheelin.repository.AnswerRepository
import com.freewheelin.repository.PieceRepository
import com.freewheelin.repository.PieceStudentRepository
import com.freewheelin.repository.PieceProblemRepository
import com.freewheelin.repository.ProblemRepository
import com.freewheelin.repository.UserRepository
import com.freewheelin.service.AnswerService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class AnswerServiceImpl(
    private val answerRepository: AnswerRepository,
    private val pieceRepository: PieceRepository,
    private val userRepository: UserRepository,
    private val problemRepository: ProblemRepository,
    private val pieceStudentRepository: PieceStudentRepository,
    private val pieceProblemRepository: PieceProblemRepository
) : AnswerService {
    
    @Transactional
    override fun scoreAnswers(pieceId: Long, studentUserId: Long, request: ScoreRequest): ScoreListResponse {
        val piece = pieceRepository.findById(pieceId)
            .orElseThrow { IllegalArgumentException(ErrorMessage.PIECE_NOT_FOUND.format(pieceId)) }
        val student = userRepository.findById(studentUserId)
            .orElseThrow { IllegalArgumentException(ErrorMessage.STUDENT_NOT_FOUND.format(studentUserId)) }

        if (student.role != UserRole.STUDENT) {
            throw IllegalArgumentException(ErrorMessage.STUDENT_ONLY_SCORE.defaultMessage)
        }

        val assignmentExists = pieceStudentRepository.findByPieceAndStudent(piece, student) != null
        if (!assignmentExists) {
            throw IllegalArgumentException(ErrorMessage.PIECE_NOT_ASSIGNED_TO_USER.defaultMessage)
        }

        val requestedProblemIds: Set<Long> = request.answers.map { it.problemId }.toSet()
        val problemsById: Map<Long, com.freewheelin.entity.Problem> =
            problemRepository.findAllById(requestedProblemIds).associateBy { it.id!! }
        val problemIdsInPiece: Set<Long> = pieceProblemRepository.findProblemIdsByPiece(piece).toSet()
        val existingAnswersByProblemId: Map<Long, Answer> = answerRepository
            .findByPieceAndStudentAndProblemIdIn(piece, student, requestedProblemIds)
            .associateBy { it.problem.id!! }

        val answersToSave: List<Answer> = request.answers.map { req ->
            val problem = problemsById[req.problemId]
                ?: throw IllegalArgumentException(ErrorMessage.PROBLEM_NOT_FOUND.format(req.problemId))

            if (problem.id !in problemIdsInPiece) {
                throw IllegalArgumentException(ErrorMessage.PIECE_PROBLEM_NOT_IN_PIECE.format(req.problemId))
            }

            require(req.problemType == problem.problemType) {
                ErrorMessage.TYPE_MISMATCH.format(req.problemId)
            }

            when (req.problemType) {
                ProblemType.SELECTION -> {
                    val n = req.studentAnswer.toIntOrNull()
                    require(n != null && n in 1..4) { ErrorMessage.SELECTION_ANSWER_INVALID.format(req.problemId) }
                }
                ProblemType.SUBJECTIVE -> {}
                ProblemType.ALL -> throw IllegalArgumentException(ErrorMessage.TYPE_ALL_NOT_ALLOWED.defaultMessage)
            }

            val isCorrect: Boolean = problem.answer == req.studentAnswer
            val existing: Answer? = existingAnswersByProblemId[problem.id]

            existing?.apply {
                this.answerText = req.studentAnswer
                this.correct = isCorrect
                this.scoredAt = LocalDateTime.now()
            } ?: Answer(
                piece = piece,
                student = student,
                problem = problem,
                answerText = req.studentAnswer,
                correct = isCorrect,
                scoredAt = LocalDateTime.now()
            )
        }

        val savedAnswers = answerRepository.saveAll(answersToSave)
        return ScoreListResponse(savedAnswers.map { it.toResponse() })
    }
}

private fun Answer.toResponse(): ScoreResponse {
    return ScoreResponse(
        pieceId = this.piece.id!!,
        studentUserId = this.student.id!!,
        problemId = this.problem.id!!,
        studentAnswer = this.answerText,
        correct = this.correct,
        scoredAt = this.scoredAt
    )
}
