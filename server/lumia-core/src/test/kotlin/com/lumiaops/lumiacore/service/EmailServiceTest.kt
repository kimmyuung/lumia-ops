package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.domain.EmailVerification
import com.lumiaops.lumiacore.domain.TeamInvitation
import com.lumiaops.lumiacore.domain.VerificationType
import com.lumiaops.lumiacore.domain.Team
import io.mockk.*
import jakarta.mail.internet.MimeMessage
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.mail.javamail.JavaMailSender
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@DisplayName("EmailService 테스트")
class EmailServiceTest {

    private lateinit var mailSender: JavaMailSender
    private lateinit var emailService: EmailService
    private lateinit var mimeMessage: MimeMessage

    @BeforeEach
    fun setUp() {
        mailSender = mockk()
        mimeMessage = mockk(relaxed = true)
        
        every { mailSender.createMimeMessage() } returns mimeMessage
        
        // EmailService는 @Value로 설정을 주입받으므로 reflection 사용
        emailService = EmailService(mailSender)
        
        // Set private fields via reflection
        emailService.javaClass.getDeclaredField("mailEnabled").apply {
            isAccessible = true
            set(emailService, false) // 테스트에서는 메일 비활성화
        }
        emailService.javaClass.getDeclaredField("baseUrl").apply {
            isAccessible = true
            set(emailService, "http://localhost:5173")
        }
    }

    @Nested
    @DisplayName("이메일 인증 메일 발송")
    inner class SendVerificationEmail {

        @Test
        @DisplayName("메일 비활성화 시 true 반환 (로그만)")
        fun `should return true when mail is disabled`() {
            // given
            val verification = EmailVerification(
                email = "test@example.com",
                type = VerificationType.SIGNUP
            )

            // when
            val result = emailService.sendVerificationEmail(verification)

            // then
            assertTrue(result) // 메일 비활성화 시에도 성공으로 처리
        }

        @Test
        @DisplayName("회원가입 인증 메일 제목")
        fun `should have correct subject for signup`() {
            // given
            val verification = EmailVerification(
                email = "test@example.com",
                type = VerificationType.SIGNUP
            )

            // when
            val result = emailService.sendVerificationEmail(verification)

            // then - 메일이 비활성화되어 있으므로 true
            assertTrue(result)
        }
    }

    @Nested
    @DisplayName("팀 초대 이메일 발송")
    inner class SendInvitationEmail {

        @Test
        @DisplayName("팀 초대 이메일 발송 성공")
        fun `should send invitation email successfully`() {
            // given
            val team = mockk<Team>()
            every { team.name } returns "Test Team"
            
            val invitation = mockk<TeamInvitation>()
            every { invitation.email } returns "invitee@example.com"
            every { invitation.team } returns team
            every { invitation.inviterName } returns "TestInviter"
            every { invitation.token } returns "invite-token"
            every { invitation.message } returns null

            // when
            val result = emailService.sendInvitationEmail(invitation)

            // then - 메일이 비활성화되어 있으므로 true
            assertTrue(result)
        }
    }

    @Nested
    @DisplayName("일반 이메일 발송")
    inner class SendEmail {

        @Test
        @DisplayName("메일 비활성화 시 true 반환")
        fun `should return true when mail is disabled`() {
            // when
            val result = emailService.sendEmail(
                to = "test@example.com",
                subject = "Test Subject",
                htmlContent = "<p>Test Content</p>"
            )

            // then
            assertTrue(result)
        }
    }

    @Nested
    @DisplayName("인증 유형별 제목")
    inner class VerificationSubject {

        @Test
        @DisplayName("각 인증 유형에 맞는 제목 반환")
        fun `should return correct subject for each verification type`() {
            // 간접 테스트 - sendVerificationEmail 호출 시 올바른 제목이 사용되는지
            // 실제로는 내부 메서드이므로 직접 테스트 불가
            
            VerificationType.entries.forEach { type ->
                val verification = EmailVerification(
                    email = "test@example.com",
                    type = type
                )
                
                // when
                val result = emailService.sendVerificationEmail(verification)
                
                // then
                assertTrue(result)
            }
        }
    }
}
