package com.freewheelin.service.impl

import com.freewheelin.dto.PieceAssignRequest
import com.freewheelin.dto.PieceAssignResponse
import com.freewheelin.entity.Piece
import com.freewheelin.entity.PieceStudent
import com.freewheelin.entity.User
import com.freewheelin.entity.UserRole
import com.freewheelin.exception.ErrorMessage
import com.freewheelin.repository.PieceRepository
import com.freewheelin.repository.PieceStudentRepository
import com.freewheelin.repository.UserRepository
import com.freewheelin.service.PieceStudentService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class PieceStudentServiceImpl(
    private val pieceStudentRepository: PieceStudentRepository,
    private val pieceRepository: PieceRepository,
    private val userRepository: UserRepository
) : PieceStudentService {
    
    @Transactional
    override fun assignPieceToStudents(pieceId: Long, teacherUserId: Long, request: PieceAssignRequest): List<PieceAssignResponse> {
        val teacher: User = userRepository.findById(teacherUserId)
            .orElseThrow { IllegalArgumentException(ErrorMessage.TEACHER_NOT_FOUND.format(teacherUserId)) }

        if (teacher.role != UserRole.TEACHER) {
            throw IllegalArgumentException(ErrorMessage.TEACHER_ONLY_ASSIGN.defaultMessage)
        }

        val piece: Piece = pieceRepository.findByCreatorAndId(teacher, pieceId)
            ?: throw IllegalArgumentException(ErrorMessage.PIECE_NOT_FOUND.format(pieceId))

        val requestedIds: Set<Long> = request.studentUserIds.toSet()

        val students: List<User> = userRepository.findAllById(requestedIds)
        val foundIds: Set<Long> = students.map { it.id!! }.toSet()
        val missing: Set<Long> = requestedIds - foundIds
        if (missing.isNotEmpty()) {
            throw IllegalArgumentException(ErrorMessage.STUDENT_NOT_FOUND.format(missing))
        }

        students.forEach { student ->
            if (student.role != UserRole.STUDENT) {
                throw IllegalArgumentException(ErrorMessage.STUDENT_NOT_FOUND.format(student.id))
            }
        }

        val existingAssignments = pieceStudentRepository.findByPieceAndStudentIn(piece, students)
        val alreadyAssignedIds: Set<Long?> = existingAssignments.map { it.student.id }.toSet()

        val studentsToAssign: List<User> = students.filter { it.id !in alreadyAssignedIds }
        if (studentsToAssign.isEmpty()) return emptyList()

        val newAssignments: List<PieceStudent> = studentsToAssign.map { student ->
            PieceStudent(piece = piece, student = student)
        }
        val saved: List<PieceStudent> = pieceStudentRepository.saveAll(newAssignments)

        return saved.map { it.toResponse() }
    }
}

private fun PieceStudent.toResponse(): PieceAssignResponse {
    return PieceAssignResponse(
        pieceId = this.piece.id!!,
        pieceName = this.piece.name,
        studentUserId = this.student.id!!,
        studentName = this.student.name,
        assignedAt = LocalDateTime.now()
    )
}
