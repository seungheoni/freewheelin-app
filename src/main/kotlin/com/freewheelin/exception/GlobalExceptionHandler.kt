package com.freewheelin.exception

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException

@RestControllerAdvice
class GlobalExceptionHandler {

    /**
     * MethodArgumentNotValidException 예외 발생시 ResponseEntity 생성 로직
     * @param ex @ModelAttribute 나 @RequestBody 유효성 검사 실패시 발생하는 예외
     */
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun methodArgumentNotValidException(ex: MethodArgumentNotValidException): ResponseEntity<ErrorBody> {
        return ResponseEntity.badRequest().body(ErrorBody.of(ex))
    }

    /**
     * ResponseStatusException 및 구현체 예외 발생시 ResponseEntity 생성 로직
     * @param ex 상태 코드와 이유를 포함하는 예외
     */
    @ExceptionHandler(ResponseStatusException::class)
    fun responseStatusException(ex: ResponseStatusException): ResponseEntity<ErrorBody> {
        return ResponseEntity.status(ex.statusCode).body(ErrorBody.of(ex))
    }

    /**
     * IllegalArgumentException 예외 발생시 ResponseEntity 생성 로직
     * @param ex 잘못된 요청 파라미터 등으로 발생하는 예외
     */
    @ExceptionHandler(IllegalArgumentException::class)
    fun illegalArgumentException(ex: IllegalArgumentException): ResponseEntity<ErrorBody> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorBody(message = ex.message))
    }

    /**
     * 기타 Exception 예외 발생시 ResponseEntity 생성 로직
     * @param ex 처리되지 않은 일반 예외
     */
    @ExceptionHandler(Exception::class)
    fun exception(ex: Exception): ResponseEntity<ErrorBody> {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ErrorBody.of())
    }
}