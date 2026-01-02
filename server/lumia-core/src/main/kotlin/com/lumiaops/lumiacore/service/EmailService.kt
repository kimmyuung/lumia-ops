package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.domain.EmailVerification
import com.lumiaops.lumiacore.domain.TeamInvitation
import com.lumiaops.lumiacore.domain.VerificationType
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.mail.javamail.JavaMailSender
import org.springframework.mail.javamail.MimeMessageHelper
import org.springframework.stereotype.Service

/**
 * 이메일 발송 서비스
 */
@Service
class EmailService(
    private val mailSender: JavaMailSender?
) {
    private val log = LoggerFactory.getLogger(javaClass)

    @Value("\${spring.mail.enabled:false}")
    private var mailEnabled: Boolean = false

    @Value("\${app.invitation.base-url:http://localhost:5173}")
    private lateinit var baseUrl: String

    @Value("\${spring.mail.username:noreply@lumiaops.com}")
    private lateinit var fromEmail: String

    /**
     * 이메일 인증 메일 발송
     */
    fun sendVerificationEmail(verification: EmailVerification): Boolean {
        val verifyLink = "$baseUrl/auth/verify?token=${verification.token}"
        val subject = getVerificationSubject(verification.type)
        val htmlContent = buildVerificationEmailHtml(verification.type, verifyLink)

        return sendEmail(verification.email, subject, htmlContent)
    }

    private fun getVerificationSubject(type: VerificationType): String {
        return when (type) {
            VerificationType.SIGNUP -> "[Lumia Ops] 회원가입 이메일 인증"
            VerificationType.PASSWORD_RESET -> "[Lumia Ops] 비밀번호 재설정"
            VerificationType.DORMANT_REACTIVATION -> "[Lumia Ops] 휴면 계정 재활성화"
            VerificationType.UNLOCK_ACCOUNT -> "[Lumia Ops] 계정 잠금 해제"
        }
    }

    private fun buildVerificationEmailHtml(type: VerificationType, verifyLink: String): String {
        val (title, description, buttonText) = when (type) {
            VerificationType.SIGNUP -> Triple(
                "회원가입 인증",
                "Lumia Ops에 가입해주셔서 감사합니다. 아래 버튼을 클릭하여 이메일을 인증해주세요.",
                "이메일 인증하기"
            )
            VerificationType.PASSWORD_RESET -> Triple(
                "비밀번호 재설정",
                "비밀번호 재설정 요청이 접수되었습니다. 아래 버튼을 클릭하여 새 비밀번호를 설정해주세요.",
                "비밀번호 재설정"
            )
            VerificationType.DORMANT_REACTIVATION -> Triple(
                "휴면 계정 재활성화",
                "6개월 이상 로그인하지 않아 휴면 계정으로 전환되었습니다. 아래 버튼을 클릭하여 계정을 재활성화해주세요.",
                "계정 재활성화"
            )
            VerificationType.UNLOCK_ACCOUNT -> Triple(
                "계정 잠금 해제",
                "로그인 시도가 5회 실패하여 계정이 잠겼습니다. 아래 버튼을 클릭하여 계정을 잠금 해제해주세요.",
                "계정 잠금 해제"
            )
        }

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
                    .container { max-width: 600px; margin: 0 auto; padding: 30px; }
                    .header { text-align: center; margin-bottom: 30px; }
                    .header h1 { color: #667eea; margin: 0; }
                    .content { background-color: #fff; padding: 30px; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .btn { display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white !important; text-decoration: none; padding: 15px 40px; border-radius: 8px; font-weight: bold; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; color: #999; font-size: 12px; }
                    .warning { color: #e53e3e; font-size: 14px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>💜 Lumia Ops</h1>
                    </div>
                    <div class="content">
                        <h2>$title</h2>
                        <p>$description</p>
                        <p style="text-align: center;">
                            <a href="$verifyLink" class="btn">$buttonText</a>
                        </p>
                        <p style="color: #666; font-size: 14px;">
                            이 링크는 15분 후 만료됩니다.<br>
                            만약 링크가 작동하지 않으면 아래 URL을 복사하여 브라우저에 붙여넣으세요:<br>
                            <a href="$verifyLink" style="color: #667eea;">$verifyLink</a>
                        </p>
                        <p class="warning">
                            ⚠️ 본인이 요청하지 않은 경우 이 이메일을 무시해주세요.
                        </p>
                    </div>
                    <div class="footer">
                        <p>© 2025 Lumia Ops. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }

    /**
     * 팀 초대 이메일 발송
     */
    fun sendInvitationEmail(invitation: TeamInvitation): Boolean {
        val inviteLink = "$baseUrl/invite/${invitation.token}"
        val teamName = invitation.team.name
        val inviterName = invitation.invitedBy.nickname ?: "팀 관리자"
        val toEmail = invitation.invitedEmail
        val customMessage = invitation.message

        val subject = "[$teamName] 팀 초대"
        val htmlContent = buildInvitationEmailHtml(
            teamName = teamName,
            inviterName = inviterName,
            inviteLink = inviteLink,
            customMessage = customMessage
        )

        return sendEmail(toEmail, subject, htmlContent)
    }

    /**
     * 일반 이메일 발송
     */
    fun sendEmail(to: String, subject: String, htmlContent: String): Boolean {
        if (!mailEnabled || mailSender == null) {
            log.info("📧 [EMAIL DISABLED] To: $to, Subject: $subject")
            log.debug("📧 Content: $htmlContent")
            return true // 개발 환경에서는 성공으로 간주
        }

        return try {
            val message = mailSender.createMimeMessage()
            val helper = MimeMessageHelper(message, true, "UTF-8")
            helper.setFrom(fromEmail)
            helper.setTo(to)
            helper.setSubject(subject)
            helper.setText(htmlContent, true)

            mailSender.send(message)
            log.info("📧 [EMAIL SENT] To: $to, Subject: $subject")
            true
        } catch (e: Exception) {
            log.error("📧 [EMAIL FAILED] To: $to, Subject: $subject, Error: ${e.message}", e)
            false
        }
    }

    private fun buildInvitationEmailHtml(
        teamName: String,
        inviterName: String,
        inviteLink: String,
        customMessage: String?
    ): String {
        val messageSection = if (!customMessage.isNullOrBlank()) {
            """
            <div style="background-color: #f5f5f5; padding: 15px; border-radius: 8px; margin: 20px 0;">
                <p style="margin: 0; color: #666; font-style: italic;">"$customMessage"</p>
            </div>
            """.trimIndent()
        } else ""

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: 'Segoe UI', Tahoma, Geneva, Verdana, sans-serif; }
                    .container { max-width: 600px; margin: 0 auto; padding: 30px; }
                    .header { text-align: center; margin-bottom: 30px; }
                    .header h1 { color: #667eea; margin: 0; }
                    .content { background-color: #fff; padding: 30px; border-radius: 12px; box-shadow: 0 2px 10px rgba(0,0,0,0.1); }
                    .btn { display: inline-block; background: linear-gradient(135deg, #667eea 0%, #764ba2 100%); color: white !important; text-decoration: none; padding: 15px 40px; border-radius: 8px; font-weight: bold; margin: 20px 0; }
                    .footer { text-align: center; margin-top: 30px; color: #999; font-size: 12px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>💜 Lumia Ops</h1>
                    </div>
                    <div class="content">
                        <h2>팀 초대</h2>
                        <p><strong>$inviterName</strong>님이 <strong>$teamName</strong> 팀에 초대했습니다.</p>
                        $messageSection
                        <p style="text-align: center;">
                            <a href="$inviteLink" class="btn">초대 수락하기</a>
                        </p>
                        <p style="color: #666; font-size: 14px;">
                            이 초대는 7일 후 만료됩니다.<br>
                            만약 링크가 작동하지 않으면 아래 URL을 복사하여 브라우저에 붙여넣으세요:<br>
                            <a href="$inviteLink" style="color: #667eea;">$inviteLink</a>
                        </p>
                    </div>
                    <div class="footer">
                        <p>© 2025 Lumia Ops. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
        """.trimIndent()
    }
}

