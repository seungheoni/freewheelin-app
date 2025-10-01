package com.freewheelin.service

import com.freewheelin.dto.PieceAssignRequest
import com.freewheelin.dto.PieceAssignResponse

interface PieceStudentService {
    fun assignPieceToStudents(pieceId: Long, teacherUserId: Long, request: PieceAssignRequest): List<PieceAssignResponse>
}
