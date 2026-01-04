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
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@DisplayName("AuthService 단위 테스트")
class AuthServiceTest {

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var emailVerificationRepository: EmailVerificationRepository

    @MockK
    private lateinit var emailService: EmailService

    @InjectMockKs
    private lateinit var authService: AuthService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Nested
    @DisplayName("registerUser 테스트")
    inner class RegisterUserTest {
        
        @Test
        @DisplayName("회원가입 성공")
        fun `should register user successfully`() {
            // given
            val email = "test@example.com"
            val password = "password123"
            val user = User(email = email, password = "encodedPassword")
            
            every { userRepository.existsByEmail(email) } returns false
            every { userRepository.save(any()) } returns user
            every { emailVerificationRepository.deleteByEmailAndTypeAndVerifiedFalse(email, VerificationType.SIGNUP) } just Runs
            every { emailVerificationRepository.save(any()) } returns EmailVerification(email = email, type = VerificationType.SIGNUP)
            every { emailService.sendVerificationEmail(any()) } returns true

            // when
            val (registeredUser, emailSent) = authService.registerUser(email, password)

            // then
            assertNotNull(registeredUser)
            assertTrue(emailSent)
            verify(exactly = 1) { userRepository.existsByEmail(email) }
            verify(exactly = 1) { userRepository.save(any()) }
        }

        @Test
        @DisplayName("중복 이메일로 회원가입 시 DuplicateException 발생")
        fun `should throw DuplicateException when email already exists`() {
            // given
            val email = "existing@example.com"
            val password = "password123"
            
            every { userRepository.existsByEmail(email) } returns true

            // when & then
            val exception = assertThrows<DuplicateException> {
                authService.registerUser(email, password)
            }
            assertEquals("DUPLICATE_EMAIL", exception.errorCode)
        }

        @Test
        @DisplayName("짧은 비밀번호로 회원가입 시 ValidationException 발생")
        fun `should throw ValidationException when password is too short`() {
            // given
            val email = "test@example.com"
            val password = "short"
            
            every { userRepository.existsByEmail(email) } returns false

            // when & then
            val exception = assertThrows<ValidationException> {
                authService.registerUser(email, password)
            }
            assertEquals("PASSWORD_TOO_SHORT", exception.errorCode)
        }

        @Test
        @DisplayName("숫자 없는 비밀번호로 회원가입 시 ValidationException 발생")
        fun `should throw ValidationException when password has no number`() {
            // given
            val email = "test@example.com"
            val password = "passwordonly"
            
            every { userRepository.existsByEmail(email) } returns false

            // when & then
            val exception = assertThrows<ValidationException> {
                authService.registerUser(email, password)
            }
            assertEquals("PASSWORD_NO_NUMBER", exception.errorCode)
        }

        @Test
        @DisplayName("영문자 없는 비밀번호로 회원가입 시 ValidationException 발생")
        fun `should throw ValidationException when password has no letter`() {
            // given
            val email = "test@example.com"
            val password = "12345678"
            
            every { userRepository.existsByEmail(email) } returns false

            // when & then
            val exception = assertThrows<ValidationException> {
                authService.registerUser(email, password)
            }
            assertEquals("PASSWORD_NO_LETTER", exception.errorCode)
        }
    }

    @Nested
    @DisplayName("verifyEmail 테스트")
    inner class VerifyEmailTest {
        
        @Test
        @DisplayName("이메일 인증 성공")
        fun `should verify email successfully`() {
            // given
            val token = "valid-token"
            val email = "test@example.com"
            val verification = mockk<EmailVerification>(relaxed = true) {
                every { this@mockk.email } returns email
                every { type } returns VerificationType.SIGNUP
                every { canVerify() } returns true
            }
            val user = User(email = email, password = "encodedPassword")
            
            every { emailVerificationRepository.findByToken(token) } returns verification
            every { userRepository.findByEmail(email) } returns user

            // when
            val result = authService.verifyEmail(token)

            // then
            assertNotNull(result)
            assertEquals(email, result.email)
            verify { verification.verify() }
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 인증 시 NotFoundException 발생")
        fun `should throw NotFoundException when token is invalid`() {
            // given
            val token = "invalid-token"
            every { emailVerificationRepository.findByToken(token) } returns null

            // when & then
            val exception = assertThrows<NotFoundException> {
                authService.verifyEmail(token)
            }
            assertEquals("TOKEN_NOT_FOUND", exception.errorCode)
        }

        @Test
        @DisplayName("만료된 토큰으로 인증 시 InvalidStateException 발생")
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
                authService.verifyEmail(token)
            }
            assertEquals("TOKEN_EXPIRED", exception.errorCode)
        }

        @Test
        @DisplayName("이미 사용된 토큰으로 인증 시 InvalidStateException 발생")
        fun `should throw InvalidStateException when token is already used`() {
            // given
            val token = "used-token"
            val verification = mockk<EmailVerification> {
                every { canVerify() } returns false
                every { isExpired() } returns false
            }
            every { emailVerificationRepository.findByToken(token) } returns verification

            // when & then
            val exception = assertThrows<InvalidStateException> {
                authService.verifyEmail(token)
            }
            assertEquals("TOKEN_USED", exception.errorCode)
        }
    }

    @Nested
    @DisplayName("login 테스트")
    inner class LoginTest {
        
        @Test
        @DisplayName("로그인 성공")
        fun `should login successfully`() {
            // given
            val email = "test@example.com"
            val password = "password123"
            val user = User(email = email, password = "\$2a\$10\$N9qo8uLOickgx2ZMRZoMy.MqpCLwN8l.SWpHnKqhMzFOp/cZsv722")
            user.verifyEmail()
            user.setInitialNickname("TestUser")
            
            every { userRepository.findByEmail(email) } returns user

            // when
            val result = authService.login(email, "password123")

            // then
            assertIs<LoginResult.Success>(result)
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 로그인 시 Failure 반환")
        fun `should return Failure when user not found`() {
            // given
            val email = "nonexistent@example.com"
            every { userRepository.findByEmail(email) } returns null

            // when
            val result = authService.login(email, "password123")

            // then
            assertIs<LoginResult.Failure>(result)
        }

        @Test
        @DisplayName("이메일 인증 대기 상태에서 로그인 시 Failure 반환")
        fun `should return Failure when email not verified`() {
            // given
            val email = "test@example.com"
            val user = User(email = email, password = "encodedPassword")
            // 이메일 인증하지 않음
            
            every { userRepository.findByEmail(email) } returns user

            // when
            val result = authService.login(email, "password123")

            // then
            assertIs<LoginResult.Failure>(result)
        }

        @Test
        @DisplayName("잠긴 계정으로 로그인 시 Locked 반환")
        fun `should return Locked when account is locked`() {
            // given
            val email = "test@example.com"
            val user = User(email = email, password = "encodedPassword")
            user.verifyEmail()
            user.setInitialNickname("TestUser")
            // 계정 잠금 (5회 실패)
            repeat(5) { user.loginFailed() }
            
            every { userRepository.findByEmail(email) } returns user

            // when
            val result = authService.login(email, "wrongpassword")

            // then
            assertIs<LoginResult.Locked>(result)
        }
    }

    @Nested
    @DisplayName("findUserById 테스트")
    inner class FindUserByIdTest {
        
        @Test
        @DisplayName("ID로 사용자 조회 성공")
        fun `should find user by id`() {
            // given
            val userId = 1L
            val user = mockk<User> {
                every { id } returns userId
            }
            every { userRepository.findById(userId) } returns Optional.of(user)

            // when
            val result = authService.findUserById(userId)

            // then
            assertNotNull(result)
            assertEquals(userId, result.id)
        }

        @Test
        @DisplayName("존재하지 않는 ID로 조회 시 null 반환")
        fun `should return null when user not found`() {
            // given
            val userId = 999L
            every { userRepository.findById(userId) } returns Optional.empty()

            // when
            val result = authService.findUserById(userId)

            // then
            assertEquals(null, result)
        }
    }
}
