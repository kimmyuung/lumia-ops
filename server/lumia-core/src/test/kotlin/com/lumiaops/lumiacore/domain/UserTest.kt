package com.lumiaops.lumiacore.domain

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

@DisplayName("User 엔티티 단위 테스트")
class UserTest {

    @Nested
    @DisplayName("User 생성 테스트")
    inner class UserCreationTest {

        @Test
        @DisplayName("User 객체 생성 성공")
        fun `should create user with valid parameters`() {
            // given
            val email = "test@example.com"
            val nickname = "테스트유저"
            val role = UserRole.USER

            // when
            val user = User(email = email, password = "encodedPassword", nickname = nickname, role = role)

            // then
            assertEquals(email, user.email)
            assertEquals(nickname, user.nickname)
            assertEquals(role, user.role)
        }

        @Test
        @DisplayName("기본 역할은 USER")
        fun `should have default role as USER`() {
            // given & when
            val user = User(email = "test@example.com", password = "encodedPassword", nickname = "테스트유저")

            // then
            assertEquals(UserRole.USER, user.role)
        }

        @Test
        @DisplayName("ADMIN 역할로 User 생성")
        fun `should create user with ADMIN role`() {
            // given & when
            val user = User(email = "admin@example.com", password = "encodedPassword", nickname = "관리자", role = UserRole.ADMIN)

            // then
            assertEquals(UserRole.ADMIN, user.role)
        }
    }

    @Nested
    @DisplayName("User 비즈니스 메서드 테스트")
    inner class UserBusinessMethodTest {

        @Test
        @DisplayName("닉네임 업데이트 성공")
        fun `should update nickname successfully`() {
            // given
            val user = User(email = "test@example.com", password = "encodedPassword", nickname = "기존닉네임").apply {
                verifyEmail()
                setInitialNickname("기존닉네임")
                // 30일 제한 우회
                val field = User::class.java.getDeclaredField("nicknameChangedAt")
                field.isAccessible = true
                field.set(this, LocalDateTime.now().minusDays(31))
            }
            val newNickname = "새닉네임"

            // when
            user.updateNickname(newNickname)

            // then
            assertEquals(newNickname, user.nickname)
        }

        @Test
        @DisplayName("닉네임을 빈 문자열로 업데이트")
        fun `should allow updating nickname to empty string`() {
            // given
            val user = User(email = "test@example.com", password = "encodedPassword", nickname = "기존닉네임").apply {
                verifyEmail()
                setInitialNickname("기존닉네임")
                // 30일 제한 우회
                val field = User::class.java.getDeclaredField("nicknameChangedAt")
                field.isAccessible = true
                field.set(this, LocalDateTime.now().minusDays(31))
            }

            // when
            user.updateNickname("")

            // then
            assertEquals("", user.nickname)
        }
    }

    @Nested
    @DisplayName("User BaseTimeEntity 상속 테스트")
    inner class UserTimeEntityTest {

        @Test
        @DisplayName("생성 시 createdAt이 설정됨")
        fun `should have createdAt when created`() {
            // given & when
            val user = User(email = "test@example.com", password = "encodedPassword", nickname = "테스트유저")

            // then
            assertNotNull(user.createdAt)
        }

        @Test
        @DisplayName("생성 시 updatedAt이 설정됨")
        fun `should have updatedAt when created`() {
            // given & when
            val user = User(email = "test@example.com", password = "encodedPassword", nickname = "테스트유저")

            // then
            assertNotNull(user.updatedAt)
        }
    }
}
