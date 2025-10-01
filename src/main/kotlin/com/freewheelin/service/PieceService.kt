package com.freewheelin.service

import com.freewheelin.dto.*

interface PieceService {
    fun createPiece(teacherUserId: Long, request: PieceCreateRequest): PieceResponse
    fun updatePieceOrder(pieceId: Long, teacherUserId: Long, request: PieceOrderUpdateRequest)
    fun getPieceProblems(pieceId: Long, studentUserId: Long): PieceProblemsResponse
    fun analyzePiece(pieceId: Long, teacherUserId: Long): PieceAnalysisResponse
}
