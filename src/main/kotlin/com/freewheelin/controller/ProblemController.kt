package com.freewheelin.controller

import com.freewheelin.dto.ProblemListResponse
import com.freewheelin.dto.ProblemSearchRequest
import com.freewheelin.service.ProblemService
import io.swagger.v3.oas.annotations.Operation
import jakarta.validation.Valid
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.bind.annotation.ModelAttribute

/**
 * 문제 관리 API
 * 선생님이 문제를 조회하고 관리하는 기능을 제공합니다.
 */
@RestController
@RequestMapping("/v1")
class ProblemController(
    private val problemService: ProblemService
) {
    
    /**
     * 문제 조회
     * 
     * 선생님이 조건에 따라 문제를 조회합니다. 
     * 난이도별 비율에 따라 문제를 선택합니다.
     *
     * @param request 문제 조회 조건
     * @return 문제 목록 응답
     */
    @GetMapping("/problems")
    @Operation(summary = "문제 조회")
    fun searchProblems(
        @ParameterObject @Valid @ModelAttribute request: ProblemSearchRequest
    ): ResponseEntity<ProblemListResponse> {
        val response = problemService.searchProblems(request)
        return ResponseEntity.ok(response)
    }
}
