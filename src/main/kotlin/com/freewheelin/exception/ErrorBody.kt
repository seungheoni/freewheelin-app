package com.freewheelin.exception

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.server.ResponseStatusException

data class ErrorBody(
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val message: String?,

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    val fields: List<SimpleFieldError>? = null,

    val timestamp: Long = System.currentTimeMillis()
) {
    companion object {
        fun of(ex: MethodArgumentNotValidException): ErrorBody {
            val fieldErrors: List<SimpleFieldError> = ex.bindingResult.fieldErrors
                .map { SimpleFieldError.of(it) }
            val topMessage: String? = ex.bindingResult.fieldErrors.firstOrNull()?.defaultMessage
                ?: "올바르지 않은 형식입니다"
            return ErrorBody(message = topMessage, fields = fieldErrors)
        }

        fun of(ex: ResponseStatusException): ErrorBody {
            return ErrorBody(message = ex.reason)
        }

        fun of(): ErrorBody {
            return ErrorBody(message = "서버 내부 오류가 발생했습니다")
        }
    }
}

data class SimpleFieldError(
    val field: String,
    val value: Any?,
    val message: String?
) {
    companion object {
        fun of(error: FieldError): SimpleFieldError {
            return SimpleFieldError(
                field = error.field,
                value = error.rejectedValue,
                message = error.defaultMessage
            )
        }
    }
}


