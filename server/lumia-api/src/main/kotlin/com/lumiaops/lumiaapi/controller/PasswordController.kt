import com.lumiaops.lumiaapi.dto.*
import com.lumiaops.lumiacore.domain.User
import com.lumiaops.lumiacore.service.PasswordService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.responses.ApiResponses
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*

/**
 * 비밀번호 관련 REST API 컨트롤러
 */
@Tag(name = "비밀번호", description = "비밀번호 찾기/재설정/변경 API")
@RestController
@RequestMapping("/auth/password")
class PasswordController(
    private val passwordService: PasswordService
) {

    @Operation(summary = "비밀번호 찾기", description = "비밀번호 재설정 이메일을 발송합니다.")
    @ApiResponse(responseCode = "200", description = "요청 처리 완료")
    @PostMapping("/forgot")
    fun forgotPassword(
        @Valid @RequestBody request: PasswordResetRequest
    ): ResponseEntity<MessageResponse> {
        return try {
            val sent = passwordService.requestPasswordReset(request.email)
            ResponseEntity.ok(MessageResponse(
                success = sent,
                message = if (sent) {
                    "비밀번호 재설정 이메일을 발송했습니다. 이메일을 확인해주세요."
                } else {
                    "이메일 발송에 실패했습니다. 다시 시도해주세요."
                }
            ))
        } catch (e: IllegalArgumentException) {
            // 보안상 존재하지 않는 이메일도 동일한 메시지 반환
            ResponseEntity.ok(MessageResponse(
                success = true,
                message = "등록된 이메일이라면 비밀번호 재설정 링크가 발송됩니다."
            ))
        }
    }

    @Operation(summary = "비밀번호 재설정", description = "토큰을 검증하고 새 비밀번호를 설정합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "재설정 성공"),
        ApiResponse(responseCode = "400", description = "재설정 실패")
    )
    @PostMapping("/reset")
    fun resetPassword(
        @Valid @RequestBody request: ResetPasswordRequest
    ): ResponseEntity<MessageResponse> {
        return try {
            passwordService.resetPassword(request.token, request.newPassword)
            ResponseEntity.ok(MessageResponse(
                success = true,
                message = "비밀번호가 재설정되었습니다. 새 비밀번호로 로그인해주세요."
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(MessageResponse(success = false, message = e.message ?: "비밀번호 재설정에 실패했습니다"))
        }
    }

    @Operation(summary = "비밀번호 변경", description = "로그인 상태에서 비밀번호를 변경합니다.")
    @ApiResponses(
        ApiResponse(responseCode = "200", description = "변경 성공"),
        ApiResponse(responseCode = "400", description = "변경 실패")
    )
    @PutMapping("/change")
    fun changePassword(
        @AuthenticationPrincipal user: User,
        @Valid @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<MessageResponse> {
        return try {
            passwordService.changePassword(user.id!!, request.oldPassword, request.newPassword)
            ResponseEntity.ok(MessageResponse(
                success = true,
                message = "비밀번호가 변경되었습니다."
            ))
        } catch (e: IllegalArgumentException) {
            ResponseEntity.badRequest()
                .body(MessageResponse(success = false, message = e.message ?: "비밀번호 변경에 실패했습니다"))
        }
    }
}
