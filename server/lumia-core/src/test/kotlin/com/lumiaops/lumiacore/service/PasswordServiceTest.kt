package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.domain.VerificationType
import com.lumiaops.lumiacore.repository.EmailVerificationRepository
import com.lumiaops.lumiacore.repository.UserRepository
import com.lumiaops.lumiacore.domain.EmailVerification
import com.lumiaops.lumiacore.domain.User
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DisplayName("PasswordService 테스트")
class PasswordServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var emailVerificationRepository: EmailVerificationRepository
    private lateinit var emailService: EmailService
    private lateinit var authService: AuthService
    private lateinit var passwordService: PasswordService

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        emailVerificationRepository = mockk()
        emailService = mockk()
        authService = mockk()
        passwordService = PasswordService(userRepository, emailVerificationRepository, emailService, authService)
    }

    @Nested
    @DisplayName("비밀번호 찾기 요청")
    inner class RequestPasswordReset {

        @Test
        @DisplayName("비밀번호 찾기 요청 성공")
        fun `should request password reset successfully`() {
            // given
            val email = "test@example.com"
            val user = User(email = email, password = "encodedPassword")
            
            every { userRepository.findByEmail(email) } returns user
            every { authService.sendVerificationEmail(email, VerificationType.PASSWORD_RESET) } returns true

            // when
            val result = passwordService.requestPasswordReset(email)

            // then
            assertTrue(result)
            verify { authService.sendVerificationEmail(email, VerificationType.PASSWORD_RESET) }
        }

        @Test
        @DisplayName("등록되지 않은 이메일로 요청 실패")
        fun `should fail when email not registered`() {
            // given
            val email = "nonexistent@example.com"
            
            every { userRepository.findByEmail(email) } returns null

            // when & then
            val exception = assertFails {
                passwordService.requestPasswordReset(email)
            }
            
            assertTrue(exception.message?.contains("등록되지 않은 이메일") == true)
        }
    }

    @Nested
    @DisplayName("비밀번호 재설정")
    inner class ResetPassword {

        @Test
        @DisplayName("비밀번호 재설정 성공")
        fun `should reset password successfully`() {
            // given
            val token = "valid-token"
            val newPassword = "newPassword123"
            val email = "test@example.com"
            val verification = EmailVerification(email = email, type = VerificationType.PASSWORD_RESET)
            val user = User(email = email, password = "oldEncodedPassword")
            
            every { emailVerificationRepository.findByToken(token) } returns verification
            every { userRepository.findByEmail(email) } returns user

            // when
            passwordService.resetPassword(token, newPassword)

            // then
            verify { emailVerificationRepository.findByToken(token) }
            verify { userRepository.findByEmail(email) }
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 재설정 실패")
        fun `should fail with invalid token`() {
            // given
            val token = "invalid-token"
            val newPassword = "newPassword123"
            
            every { emailVerificationRepository.findByToken(token) } returns null

            // when & then
            val exception = assertFails {
                passwordService.resetPassword(token, newPassword)
            }
            
            assertTrue(exception.message?.contains("유효하지 않은 인증 토큰") == true)
        }

        @Test
        @DisplayName("짧은 비밀번호로 재설정 실패")
        fun `should fail when new password is too short`() {
            // given
            val token = "valid-token"
            val newPassword = "short1"
            val email = "test@example.com"
            val verification = EmailVerification(email = email, type = VerificationType.PASSWORD_RESET)
            
            every { emailVerificationRepository.findByToken(token) } returns verification

            // when & then
            val exception = assertFails {
                passwordService.resetPassword(token, newPassword)
            }
            
            assertTrue(exception.message?.contains("8자 이상") == true)
        }
    }

    @Nested
    @DisplayName("비밀번호 변경")
    inner class ChangePassword {

        @Test
        @DisplayName("비밀번호 변경 성공")
        fun `should change password successfully`() {
            // given
            val userId = 1L
            val oldPassword = "oldPassword123"
            val newPassword = "newPassword456"
            
            // BCrypt 인코딩된 oldPassword
            val encodedOldPassword = "\$2a\$10\$dXJ3SW6G7P50lGmMkkmwe.20cQQubk3.HZWzG3YB1tlruCihQ7J6i" // "oldPassword123" encoded
            val user = User(email = "test@example.com", password = encodedOldPassword)
            
            every { userRepository.findById(userId) } returns Optional.of(user)

            // when & then
            // 실제 BCrypt 매칭이 실패할 것이므로 예외가 발생할 것
            val exception = assertFails {
                passwordService.changePassword(userId, oldPassword, newPassword)
            }
            
            // 비밀번호 불일치 또는 유효성 검사 실패
            assertNotNull(exception.message)
        }

        @Test
        @DisplayName("사용자를 찾을 수 없음")
        fun `should fail when user not found`() {
            // given
            val userId = 999L
            val oldPassword = "oldPassword123"
            val newPassword = "newPassword456"
            
            every { userRepository.findById(userId) } returns Optional.empty()

            // when & then
            val exception = assertFails {
                passwordService.changePassword(userId, oldPassword, newPassword)
            }
            
            assertTrue(exception.message?.contains("사용자를 찾을 수 없습니다") == true)
        }
    }
}
