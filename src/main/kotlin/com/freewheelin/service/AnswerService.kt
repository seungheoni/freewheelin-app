package com.freewheelin.service

import com.freewheelin.dto.ScoreListResponse
import com.freewheelin.dto.ScoreRequest

interface AnswerService {
    fun scoreAnswers(pieceId: Long, studentUserId: Long, request: ScoreRequest): ScoreListResponse
}
