package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.domain.User
import com.lumiaops.lumiacore.domain.UserRole
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
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@DisplayName("UserService 단위 테스트")
class UserServiceTest {

    @MockK
    private lateinit var userRepository: UserRepository

    @InjectMockKs
    private lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Nested
    @DisplayName("findById 테스트")
    inner class FindByIdTest {
        @Test
        @DisplayName("존재하는 사용자 ID로 조회 시 사용자 반환")
        fun `should return user when user exists`() {
            // given
            val userId = 1L
            val user = mockk<User> {
                every { id } returns userId
                every { email } returns "test@example.com"
                every { nickname } returns "테스트유저"
            }
            every { userRepository.findById(userId) } returns Optional.of(user)

            // when
            val result = userService.findById(userId)

            // then
            assertNotNull(result)
            assertEquals(userId, result.id)
            verify(exactly = 1) { userRepository.findById(userId) }
        }

        @Test
        @DisplayName("존재하지 않는 사용자 ID로 조회 시 null 반환")
        fun `should return null when user does not exist`() {
            // given
            val userId = 999L
            every { userRepository.findById(userId) } returns Optional.empty()

            // when
            val result = userService.findById(userId)

            // then
            assertNull(result)
            verify(exactly = 1) { userRepository.findById(userId) }
        }
    }

    @Nested
    @DisplayName("findByEmail 테스트")
    inner class FindByEmailTest {
        @Test
        @DisplayName("존재하는 이메일로 조회 시 사용자 반환")
        fun `should return user when email exists`() {
            // given
            val email = "test@example.com"
            val user = mockk<User> {
                every { this@mockk.email } returns email
            }
            every { userRepository.findByEmail(email) } returns user

            // when
            val result = userService.findByEmail(email)

            // then
            assertNotNull(result)
            assertEquals(email, result.email)
            verify(exactly = 1) { userRepository.findByEmail(email) }
        }
    }

    @Nested
    @DisplayName("existsByEmail 테스트")
    inner class ExistsByEmailTest {
        @Test
        @DisplayName("이메일이 존재하면 true 반환")
        fun `should return true when email exists`() {
            // given
            val email = "existing@example.com"
            every { userRepository.existsByEmail(email) } returns true

            // when
            val result = userService.existsByEmail(email)

            // then
            assertTrue(result)
            verify(exactly = 1) { userRepository.existsByEmail(email) }
        }
    }

    @Nested
    @DisplayName("createUser 테스트")
    inner class CreateUserTest {
        @Test
        @DisplayName("새 사용자 생성 성공")
        fun `should create user when email is unique`() {
            // given
            val email = "new@example.com"
            val nickname = "새사용자"
            val savedUser = User(email = email, nickname = nickname, role = UserRole.USER)
            
            every { userRepository.existsByEmail(email) } returns false
            every { userRepository.save(any<User>()) } returns savedUser

            // when
            val result = userService.createUser(email, nickname)

            // then
            assertEquals(email, result.email)
            assertEquals(nickname, result.nickname)
            verify(exactly = 1) { userRepository.existsByEmail(email) }
            verify(exactly = 1) { userRepository.save(any<User>()) }
        }

        @Test
        @DisplayName("중복 이메일로 사용자 생성 시 예외 발생")
        fun `should throw exception when email already exists`() {
            // given
            val email = "existing@example.com"
            val nickname = "중복사용자"
            every { userRepository.existsByEmail(email) } returns true

            // when & then
            val exception = assertThrows<IllegalArgumentException> {
                userService.createUser(email, nickname)
            }
            assertEquals("이미 존재하는 이메일입니다: $email", exception.message)
            verify(exactly = 1) { userRepository.existsByEmail(email) }
            verify(exactly = 0) { userRepository.save(any<User>()) }
        }
    }

    @Nested
    @DisplayName("updateNickname 테스트")
    inner class UpdateNicknameTest {
        @Test
        @DisplayName("닉네임 업데이트 성공")
        fun `should update nickname when user exists`() {
            // given
            val userId = 1L
            val newNickname = "새닉네임"
            val user = User(email = "test@example.com", nickname = "기존닉네임")
            
            every { userRepository.findById(userId) } returns Optional.of(user)

            // when
            val result = userService.updateNickname(userId, newNickname)

            // then
            assertEquals(newNickname, result.nickname)
            verify(exactly = 1) { userRepository.findById(userId) }
        }

        @Test
        @DisplayName("존재하지 않는 사용자 닉네임 업데이트 시 예외 발생")
        fun `should throw exception when user does not exist`() {
            // given
            val userId = 999L
            val newNickname = "새닉네임"
            every { userRepository.findById(userId) } returns Optional.empty()

            // when & then
            val exception = assertThrows<IllegalArgumentException> {
                userService.updateNickname(userId, newNickname)
            }
            assertEquals("사용자를 찾을 수 없습니다: $userId", exception.message)
        }
    }

    @Nested
    @DisplayName("deleteUser 테스트")
    inner class DeleteUserTest {
        @Test
        @DisplayName("사용자 삭제 성공")
        fun `should delete user successfully`() {
            // given
            val userId = 1L
            every { userRepository.deleteById(userId) } just Runs

            // when
            userService.deleteUser(userId)

            // then
            verify(exactly = 1) { userRepository.deleteById(userId) }
        }
    }

    @Nested
    @DisplayName("findAll 테스트")
    inner class FindAllTest {
        @Test
        @DisplayName("모든 사용자 조회 성공")
        fun `should return all users`() {
            // given
            val users = listOf(
                User(email = "user1@example.com", nickname = "유저1"),
                User(email = "user2@example.com", nickname = "유저2")
            )
            every { userRepository.findAll() } returns users

            // when
            val result = userService.findAll()

            // then
            assertEquals(2, result.size)
            verify(exactly = 1) { userRepository.findAll() }
        }
    }
}
