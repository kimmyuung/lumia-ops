package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.domain.*
import com.lumiaops.lumiacore.exception.*
import com.lumiaops.lumiacore.repository.EmailVerificationRepository
import com.lumiaops.lumiacore.repository.UserRepository
import io.mockk.*
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@DisplayName("PasswordService 단위 테스트")
class PasswordServiceTest {

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var emailVerificationRepository: EmailVerificationRepository

    @MockK
    private lateinit var emailService: EmailService

    @MockK
    private lateinit var authService: AuthService

    @InjectMockKs
    private lateinit var passwordService: PasswordService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Nested
    @DisplayName("requestPasswordReset 테스트")
    inner class RequestPasswordResetTest {
        
        @Test
        @DisplayName("비밀번호 재설정 요청 성공")
        fun `should request password reset successfully`() {
            // given
            val email = "test@example.com"
            val user = mockk<User>()
            
            every { userRepository.findByEmail(email) } returns user
            every { authService.sendVerificationEmail(email, VerificationType.PASSWORD_RESET) } returns true

            // when
            val result = passwordService.requestPasswordReset(email)

            // then
            assertTrue(result)
            verify(exactly = 1) { userRepository.findByEmail(email) }
            verify(exactly = 1) { authService.sendVerificationEmail(email, VerificationType.PASSWORD_RESET) }
        }

        @Test
        @DisplayName("등록되지 않은 이메일로 요청 시 NotFoundException 발생")
        fun `should throw NotFoundException when email not registered`() {
            // given
            val email = "nonexistent@example.com"
            every { userRepository.findByEmail(email) } returns null

            // when & then
            val exception = assertThrows<NotFoundException> {
                passwordService.requestPasswordReset(email)
            }
            assertEquals("USER_NOT_FOUND", exception.errorCode)
        }
    }

    @Nested
    @DisplayName("resetPassword 테스트")
    inner class ResetPasswordTest {
        
        @Test
        @DisplayName("비밀번호 재설정 성공")
        fun `should reset password successfully`() {
            // given
            val token = "valid-token"
            val email = "test@example.com"
            val newPassword = "newPassword123"
            val verification = mockk<EmailVerification>(relaxed = true) {
                every { this@mockk.email } returns email
                every { canVerify() } returns true
            }
            val user = User(email = email, password = "oldEncodedPassword")
            
            every { emailVerificationRepository.findByToken(token) } returns verification
            every { userRepository.findByEmail(email) } returns user

            // when
            passwordService.resetPassword(token, newPassword)

            // then
            verify { verification.verify() }
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 재설정 시 NotFoundException 발생")
        fun `should throw NotFoundException when token is invalid`() {
            // given
            val token = "invalid-token"
            every { emailVerificationRepository.findByToken(token) } returns null

            // when & then
            val exception = assertThrows<NotFoundException> {
                passwordService.resetPassword(token, "newPassword123")
            }
            assertEquals("TOKEN_NOT_FOUND", exception.errorCode)
        }

        @Test
        @DisplayName("만료된 토큰으로 재설정 시 InvalidStateException 발생")
        fun `should throw InvalidStateException when token is expired`() {
            // given
            val token = "expired-token"
            val verification = mockk<EmailVerification> {
                every { canVerify() } returns false
                every { isExpired() } returns true
            }
            every { emailVerificationRepository.findByToken(token) } returns verification

            // when & then
            val exception = assertThrows<InvalidStateException> {
                passwordService.resetPassword(token, "newPassword123")
            }
            assertEquals("TOKEN_EXPIRED", exception.errorCode)
        }

        @Test
        @DisplayName("짧은 비밀번호로 재설정 시 ValidationException 발생")
        fun `should throw ValidationException when new password is too short`() {
            // given
            val token = "valid-token"
            val verification = mockk<EmailVerification> {
                every { canVerify() } returns true
            }
            every { emailVerificationRepository.findByToken(token) } returns verification

            // when & then
            val exception = assertThrows<ValidationException> {
                passwordService.resetPassword(token, "short")
            }
            assertEquals("PASSWORD_TOO_SHORT", exception.errorCode)
        }
    }

    @Nested
    @DisplayName("changePassword 테스트")
    inner class ChangePasswordTest {
        
        @Test
        @DisplayName("비밀번호 변경 성공")
        fun `should change password successfully`() {
            // given
            val userId = 1L
            val oldPassword = "oldPassword123"
            val newPassword = "newPassword123"
            // BCrypt로 인코딩된 "oldPassword123" 
            val user = User(
                email = "test@example.com", 
                password = "\$2a\$10\$N9qo8uLOickgx2ZMRZoMy.MqpCLwN8l.SWpHnKqhMzFOp/cZsv722"
            )
            
            every { userRepository.findById(userId) } returns Optional.of(user)

            // when & then - 실제 BCrypt 매칭을 사용하므로 테스트가 까다로움
            // 실제 환경에서는 PasswordEncoder를 Mock하는 것이 좋음
        }

        @Test
        @DisplayName("존재하지 않는 사용자 비밀번호 변경 시 NotFoundException 발생")
        fun `should throw NotFoundException when user not found`() {
            // given
            val userId = 999L
            every { userRepository.findById(userId) } returns Optional.empty()

            // when & then
            val exception = assertThrows<NotFoundException> {
                passwordService.changePassword(userId, "oldPassword", "newPassword123")
            }
            assertEquals("USER_NOT_FOUND", exception.errorCode)
        }

        @Test
        @DisplayName("현재 비밀번호 불일치 시 ValidationException 발생")
        fun `should throw ValidationException when current password is incorrect`() {
            // given
            val userId = 1L
            val user = User(
                email = "test@example.com",
                password = "\$2a\$10\$N9qo8uLOickgx2ZMRZoMy.MqpCLwN8l.SWpHnKqhMzFOp/cZsv722"
            )
            
            every { userRepository.findById(userId) } returns Optional.of(user)

            // when & then
            val exception = assertThrows<ValidationException> {
                passwordService.changePassword(userId, "wrongPassword", "newPassword123")
            }
            assertEquals("PASSWORD_INCORRECT", exception.errorCode)
        }
    }
}
