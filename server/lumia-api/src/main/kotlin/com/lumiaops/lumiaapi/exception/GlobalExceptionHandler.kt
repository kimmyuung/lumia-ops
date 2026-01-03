package com.lumiaops.lumiaapi.exception

import com.lumiaops.lumiaapi.dto.ErrorResponse
import com.lumiaops.lumiaapi.dto.FieldError
import com.lumiaops.lumiacore.exception.*
import jakarta.servlet.http.HttpServletRequest
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException
import org.springframework.web.server.ResponseStatusException

/**
 * 전역 예외 핸들러
 * 모든 예외를 일관된 형식으로 처리
 */
@RestControllerAdvice
class GlobalExceptionHandler {

    private val log = LoggerFactory.getLogger(javaClass)

    // ==================== 도메인 예외 ====================

    @ExceptionHandler(NotFoundException::class)
    fun handleNotFoundException(
        ex: NotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.debug("NotFoundException: ${ex.message}")
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
            ErrorResponse.of(
                status = 404,
                error = "Not Found",
                code = ex.errorCode,
                message = ex.message,
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(DuplicateException::class)
    fun handleDuplicateException(
        ex: DuplicateException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.debug("DuplicateException: ${ex.message}")
        return ResponseEntity.status(HttpStatus.CONFLICT).body(
            ErrorResponse.of(
                status = 409,
                error = "Conflict",
                code = ex.errorCode,
                message = ex.message,
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(InvalidStateException::class)
    fun handleInvalidStateException(
        ex: InvalidStateException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.debug("InvalidStateException: ${ex.message}")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse.of(
                status = 400,
                error = "Bad Request",
                code = ex.errorCode,
                message = ex.message,
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(ValidationException::class)
    fun handleValidationException(
        ex: ValidationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.debug("ValidationException: ${ex.message}")
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(
            ErrorResponse(
                status = 422,
                error = "Unprocessable Entity",
                code = ex.errorCode,
                message = ex.message,
                path = request.requestURI,
                field = ex.field
            )
        )
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(
        ex: AuthenticationException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.debug("AuthenticationException: ${ex.message}")
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
            ErrorResponse.of(
                status = 401,
                error = "Unauthorized",
                code = ex.errorCode,
                message = ex.message,
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(ForbiddenException::class)
    fun handleForbiddenException(
        ex: ForbiddenException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.debug("ForbiddenException: ${ex.message}")
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(
            ErrorResponse.of(
                status = 403,
                error = "Forbidden",
                code = ex.errorCode,
                message = ex.message,
                path = request.requestURI
            )
        )
    }

    // ==================== Spring 예외 ====================

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationErrors(
        ex: MethodArgumentNotValidException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.fieldErrors.map {
            FieldError(
                field = it.field,
                message = it.defaultMessage ?: "유효하지 않은 값입니다",
                rejectedValue = it.rejectedValue
            )
        }
        log.debug("MethodArgumentNotValidException: ${errors.size} errors")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(
                status = 400,
                error = "Bad Request",
                code = "VALIDATION_ERROR",
                message = "입력값 검증에 실패했습니다",
                path = request.requestURI,
                details = errors
            )
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadableException(
        ex: HttpMessageNotReadableException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.debug("HttpMessageNotReadableException: ${ex.message}")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse.of(
                status = 400,
                error = "Bad Request",
                code = "MALFORMED_REQUEST",
                message = "요청 본문을 읽을 수 없습니다",
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingServletRequestParameterException(
        ex: MissingServletRequestParameterException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.debug("MissingServletRequestParameterException: ${ex.parameterName}")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(
                status = 400,
                error = "Bad Request",
                code = "MISSING_PARAMETER",
                message = "필수 파라미터가 누락되었습니다: ${ex.parameterName}",
                path = request.requestURI,
                field = ex.parameterName
            )
        )
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException::class)
    fun handleMethodArgumentTypeMismatchException(
        ex: MethodArgumentTypeMismatchException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.debug("MethodArgumentTypeMismatchException: ${ex.name}")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse(
                status = 400,
                error = "Bad Request",
                code = "TYPE_MISMATCH",
                message = "파라미터 타입이 올바르지 않습니다: ${ex.name}",
                path = request.requestURI,
                field = ex.name
            )
        )
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(
        ex: HttpRequestMethodNotSupportedException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.debug("HttpRequestMethodNotSupportedException: ${ex.method}")
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(
            ErrorResponse.of(
                status = 405,
                error = "Method Not Allowed",
                code = "METHOD_NOT_ALLOWED",
                message = "지원하지 않는 HTTP 메서드입니다: ${ex.method}",
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(ResponseStatusException::class)
    fun handleResponseStatusException(
        ex: ResponseStatusException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.debug("ResponseStatusException: ${ex.statusCode} - ${ex.reason}")
        return ResponseEntity.status(ex.statusCode).body(
            ErrorResponse.of(
                status = ex.statusCode.value(),
                error = ex.statusCode.toString(),
                code = "HTTP_ERROR",
                message = ex.reason ?: "요청을 처리할 수 없습니다",
                path = request.requestURI
            )
        )
    }

    // ==================== 기타 예외 (하위 호환) ====================

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        ex: IllegalArgumentException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn("IllegalArgumentException (레거시): ${ex.message}")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse.of(
                status = 400,
                error = "Bad Request",
                code = "ILLEGAL_ARGUMENT",
                message = ex.message ?: "잘못된 요청입니다",
                path = request.requestURI
            )
        )
    }

    @ExceptionHandler(IllegalStateException::class)
    fun handleIllegalStateException(
        ex: IllegalStateException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.warn("IllegalStateException (레거시): ${ex.message}")
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
            ErrorResponse.of(
                status = 400,
                error = "Bad Request",
                code = "ILLEGAL_STATE",
                message = ex.message ?: "잘못된 상태입니다",
                path = request.requestURI
            )
        )
    }

    // ==================== 예상치 못한 예외 ====================

    @ExceptionHandler(Exception::class)
    fun handleAllExceptions(
        ex: Exception,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        log.error("Unexpected exception: ${ex.javaClass.simpleName}", ex)
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse.of(
                status = 500,
                error = "Internal Server Error",
                code = "INTERNAL_ERROR",
                message = "서버 오류가 발생했습니다",
                path = request.requestURI
            )
        )
    }
}
