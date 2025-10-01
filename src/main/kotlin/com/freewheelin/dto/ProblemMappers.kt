package com.freewheelin.dto

import com.freewheelin.entity.Problem

fun Problem.toResponse(): ProblemResponse = ProblemResponse(
    id = this.id!!,
    answer = this.answer ?: "",
    unitCode = this.unitCode.code,
    level = this.level,
    problemType = this.problemType.name
)


