package com.freewheelin.controller

import com.freewheelin.dto.*
import com.freewheelin.service.PieceService
import com.freewheelin.service.PieceStudentService
import com.freewheelin.service.AnswerService
import io.swagger.v3.oas.annotations.Operation
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

/**
 * 학습지 관리 API
 * 선생님이 학습지를 생성하고 관리하는 기능을 제공합니다.
 */
@RestController
@RequestMapping("/v1/piece")
class PieceController(
    private val pieceService: PieceService,
    private val pieceStudentService: PieceStudentService,
    private val answerService: AnswerService
) {
    
    /**
     * 학습지 생성
     * 
     * 선생님이 학습지를 생성합니다. 
     * 최대 50개 문제까지 포함 가능합니다.
     * 
     * @param teacherUserId 선생님 ID
     * @param request 학습지 생성 요청
     * @return 생성된 학습지 정보
     */
    @PostMapping
    @Operation(summary = "학습지 생성")
    fun createPiece(
        @RequestParam teacherUserId: Long,
        @RequestBody @Valid request: PieceCreateRequest
    ): ResponseEntity<PieceResponse> {
        val response = pieceService.createPiece(teacherUserId, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }
    
    /**
     * 학습지 문제 순서 수정
     *
     * 프론트가 계산한 결과로, 화면에 보이는 전체 problemId의 최종 순서를 한 번에 갱신합니다.
     * 처리만 수행하고 본문 없는 204(No Content)를 반환합니다.
     *
     * @param pieceId 학습지 ID
     * @param teacherUserId 선생님 ID
     * @param request {orderedProblemIds: Long[]}
     */
    @PatchMapping("/{pieceId}/order")
    @Operation(summary = "학습지 문제 순서 수정")
    fun updatePieceOrder(
        @PathVariable pieceId: Long,
        @RequestParam teacherUserId: Long,
        @RequestBody @Valid request: PieceOrderUpdateRequest
    ): ResponseEntity<Void> {
        pieceService.updatePieceOrder(pieceId, teacherUserId, request)
        return ResponseEntity.noContent().build()
    }
    
    /**
     * 학생에게 학습지 출제
     *
     * 선생님이 특정 학습지(`pieceId`)를 하나 이상의 학생에게 출제합니다.
     * 이미 배정된 학생은 건너뛰고, 새로 배정된 학생들만 응답에 포함됩니다.
     *
     * @param pieceId 학습지 ID
     * @param teacherUserId 선생님 ID (권한 검증용)
     * @param request 배정 대상 학생 목록 요청
     * @return 새로 배정된 학생들 정보 목록
     *
     */
    @PostMapping("/{pieceId}")
    @Operation(summary = "학생에게 학습지 출제")
    fun assignPieceToStudents(
        @PathVariable pieceId: Long,
        @RequestParam teacherUserId: Long,
        @RequestParam(name = "studentIds") studentUserIds: List<Long>
    ): ResponseEntity<List<PieceAssignResponse>> {
        val request = PieceAssignRequest(studentUserIds = studentUserIds)
        val response = pieceStudentService.assignPieceToStudents(pieceId, teacherUserId, request)
        return ResponseEntity.ok(response)
    }

    /**
     * 학습지 채점
     *
     * 학생이 본인의 학습지에 대해 여러 문제를 일괄 채점합니다.
     * 문제 유형 불일치, 미배정 학습지, 학습지 외 문제 제출 등은 에러로 처리됩니다.
     *
     * @param pieceId 학습지 ID
     * @param studentUserId 학생 ID (배정 검증 및 소유 검증)
     * @param request 채점할 문제 목록과 학생 답안
     * @return 채점 결과 목록
     *
     */
    @PutMapping("/{pieceId}/score")
    @Operation(summary = "학습지 채점")
    fun scoreAnswers(
        @PathVariable pieceId: Long,
        @RequestParam studentUserId: Long,
        @RequestBody @Valid request: ScoreRequest
    ): ResponseEntity<ScoreListResponse> {
        val response = answerService.scoreAnswers(pieceId, studentUserId, request)
        return ResponseEntity.ok(response)
    }
    
    /**
     * 학습지 문제 조회
     * 
     * 학습지에 포함된 문제 목록을 조회합니다.
     * 
     * @param pieceId 학습지 ID
     * @return 학습지 문제 목록
     *
     */
    @GetMapping("/{pieceId}/problems")
    @Operation(summary = "학습지 문제 조회")
    fun getPieceProblems(
        @PathVariable pieceId: Long,
        @RequestParam studentUserId: Long
    ): ResponseEntity<PieceProblemsResponse> {
        val response = pieceService.getPieceProblems(pieceId, studentUserId)
        return ResponseEntity.ok(response)
    }
    
    /**
     * 학습지 학습 통계 분석
     * 
     * 학습지에 대한 학생들의 학습 통계를 분석합니다.
     * 
     * @param pieceId 학습지 ID
     * @param teacherUserId 선생님 ID
     * @return 학습지 통계 분석 결과
     *
     */
    @GetMapping("/{pieceId}/analyze")
    @Operation(summary = "학습지 학습 통계 분석")
    fun analyzePiece(
        @PathVariable pieceId: Long,
        @RequestParam teacherUserId: Long
    ): ResponseEntity<PieceAnalysisResponse> {
        val response = pieceService.analyzePiece(pieceId, teacherUserId)
        return ResponseEntity.ok(response)
    }
}
