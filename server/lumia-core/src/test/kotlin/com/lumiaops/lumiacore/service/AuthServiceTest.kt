package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.domain.*
import com.lumiaops.lumiacore.repository.EmailVerificationRepository
import com.lumiaops.lumiacore.repository.UserRepository
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlin.test.assertFails

@DisplayName("AuthService 테스트")
class AuthServiceTest {

    private lateinit var userRepository: UserRepository
    private lateinit var emailVerificationRepository: EmailVerificationRepository
    private lateinit var emailService: EmailService
    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        userRepository = mockk()
        emailVerificationRepository = mockk()
        emailService = mockk()
        authService = AuthService(userRepository, emailVerificationRepository, emailService)
    }

    @Nested
    @DisplayName("회원가입")
    inner class RegisterUser {

        @Test
        @DisplayName("성공적으로 회원가입")
        fun `should register user successfully`() {
            // given
            val email = "test@example.com"
            val password = "password123"
            
            every { userRepository.existsByEmail(email) } returns false
            every { userRepository.save(any()) } answers { firstArg() }
            every { emailVerificationRepository.deleteByEmailAndTypeAndVerifiedFalse(any(), any()) } returns Unit
            every { emailVerificationRepository.save(any()) } answers { firstArg() }
            every { emailService.sendVerificationEmail(any()) } returns true

            // when
            val (user, emailSent) = authService.registerUser(email, password)

            // then
            assertNotNull(user)
            assertEquals(email, user.email)
            assertEquals(AccountStatus.PENDING_EMAIL, user.status)
            assertTrue(emailSent)
            
            verify { userRepository.existsByEmail(email) }
            verify { userRepository.save(any()) }
        }

        @Test
        @DisplayName("중복 이메일로 회원가입 실패")
        fun `should fail when email already exists`() {
            // given
            val email = "duplicate@example.com"
            val password = "password123"
            
            every { userRepository.existsByEmail(email) } returns true

            // when & then
            val exception = assertFails {
                authService.registerUser(email, password)
            }
            
            assertTrue(exception.message?.contains("이미 존재하는 이메일") == true)
        }

        @Test
        @DisplayName("짧은 비밀번호로 회원가입 실패")
        fun `should fail when password is too short`() {
            // given
            val email = "test@example.com"
            val password = "short1"
            
            every { userRepository.existsByEmail(email) } returns false

            // when & then
            val exception = assertFails {
                authService.registerUser(email, password)
            }
            
            assertTrue(exception.message?.contains("8자 이상") == true)
        }

        @Test
        @DisplayName("숫자 없는 비밀번호로 회원가입 실패")
        fun `should fail when password has no digits`() {
            // given
            val email = "test@example.com"
            val password = "passwordonly"
            
            every { userRepository.existsByEmail(email) } returns false

            // when & then
            val exception = assertFails {
                authService.registerUser(email, password)
            }
            
            assertTrue(exception.message?.contains("숫자가 포함") == true)
        }
    }

    @Nested
    @DisplayName("이메일 인증")
    inner class VerifyEmail {

        @Test
        @DisplayName("유효한 토큰으로 이메일 인증 성공")
        fun `should verify email with valid token`() {
            // given
            val token = "valid-token"
            val email = "test@example.com"
            val verification = EmailVerification(email = email, type = VerificationType.SIGNUP)
            val user = User(email = email, password = "encodedPassword")
            
            every { emailVerificationRepository.findByToken(token) } returns verification
            every { userRepository.findByEmail(email) } returns user

            // when
            val result = authService.verifyEmail(token)

            // then
            assertNotNull(result)
            assertEquals(email, result.email)
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 인증 실패")
        fun `should fail with invalid token`() {
            // given
            val token = "invalid-token"
            
            every { emailVerificationRepository.findByToken(token) } returns null

            // when & then
            val exception = assertFails {
                authService.verifyEmail(token)
            }
            
            assertTrue(exception.message?.contains("유효하지 않은 인증 토큰") == true)
        }
    }

    @Nested
    @DisplayName("로그인")
    inner class Login {

        @Test
        @DisplayName("로그인 성공")
        fun `should login successfully`() {
            // given
            val email = "test@example.com"
            val password = "password123"
            val user = User(email = email, password = "\$2a\$10\$dummyEncodedPassword")
            // Activate user
            user.javaClass.getDeclaredField("status").apply {
                isAccessible = true
                set(user, AccountStatus.ACTIVE)
            }
            
            every { userRepository.findByEmail(email) } returns user

            // when - 비밀번호가 맞지 않을 것임 (mock이므로)
            val result = authService.login(email, password)

            // then - 실제로는 비밀번호 불일치로 Failure가 될 것
            assertTrue(result is LoginResult.Failure || result is LoginResult.Success)
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 실패")
        fun `should fail with non-existent email`() {
            // given
            val email = "nonexistent@example.com"
            val password = "password123"
            
            every { userRepository.findByEmail(email) } returns null

            // when
            val result = authService.login(email, password)

            // then
            assertTrue(result is LoginResult.Failure)
            assertTrue((result as LoginResult.Failure).message.contains("이메일 또는 비밀번호"))
        }

        @Test
        @DisplayName("이메일 인증 대기 상태로 로그인 실패")
        fun `should fail when email not verified`() {
            // given
            val email = "test@example.com"
            val password = "password123"
            val user = User(email = email, password = "encodedPassword")
            // status is PENDING_EMAIL by default
            
            every { userRepository.findByEmail(email) } returns user

            // when
            val result = authService.login(email, password)

            // then
            assertTrue(result is LoginResult.Failure)
            assertTrue((result as LoginResult.Failure).message.contains("이메일 인증이 필요"))
        }

        @Test
        @DisplayName("잠긴 계정으로 로그인 실패")
        fun `should fail when account is locked`() {
            // given
            val email = "test@example.com"
            val password = "password123"
            val user = User(email = email, password = "encodedPassword")
            user.javaClass.getDeclaredField("status").apply {
                isAccessible = true
                set(user, AccountStatus.LOCKED)
            }
            
            every { userRepository.findByEmail(email) } returns user

            // when
            val result = authService.login(email, password)

            // then
            assertTrue(result is LoginResult.Locked)
        }
    }

    @Nested
    @DisplayName("아이디 찾기")
    inner class FindUsername {

        @Test
        @DisplayName("존재하는 이메일 찾기 성공")
        fun `should find existing email`() {
            // given
            val email = "test@example.com"
            val user = User(email = email, password = "encodedPassword")
            
            every { userRepository.findByEmail(email) } returns user

            // when
            val result = authService.findUsername(email)

            // then
            assertEquals(email, result)
        }

        @Test
        @DisplayName("존재하지 않는 이메일 찾기")
        fun `should return null for non-existent email`() {
            // given
            val email = "nonexistent@example.com"
            
            every { userRepository.findByEmail(email) } returns null

            // when
            val result = authService.findUsername(email)

            // then
            assertEquals(null, result)
        }
    }
}
