package com.freewheelin.service

import com.freewheelin.dto.ProblemListResponse
import com.freewheelin.dto.ProblemSearchRequest

interface ProblemService {
    fun searchProblems(request: ProblemSearchRequest): ProblemListResponse
}
