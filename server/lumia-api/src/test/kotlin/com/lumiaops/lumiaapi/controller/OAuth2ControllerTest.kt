package com.lumiaops.lumiaapi.controller

import com.fasterxml.jackson.databind.ObjectMapper
import com.lumiaops.lumiaapi.dto.SteamLoginRequest
import com.lumiaops.lumiaapi.dto.KakaoLoginRequest
import com.lumiaops.lumiaapi.dto.SetupGameNicknameRequest
import com.lumiaops.lumiacore.domain.User
import com.lumiaops.lumiacore.external.PlayerStatsData
import com.lumiaops.lumiacore.security.JwtTokenProvider
import com.lumiaops.lumiacore.service.OAuth2AuthService
import com.lumiaops.lumiacore.service.PlayerStatsService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders

@DisplayName("OAuth2Controller Test")
class OAuth2ControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var oAuth2AuthService: OAuth2AuthService
    private lateinit var jwtTokenProvider: JwtTokenProvider
    private lateinit var playerStatsService: PlayerStatsService
    private lateinit var objectMapper: ObjectMapper

    @BeforeEach
    fun setUp() {
        oAuth2AuthService = mockk()
        jwtTokenProvider = mockk()
        playerStatsService = mockk()
        objectMapper = ObjectMapper()

        val controller = OAuth2Controller(oAuth2AuthService, jwtTokenProvider, playerStatsService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller).build()
    }

    @Nested
    @DisplayName("Steam Login Callback")
    inner class SteamCallback {

        @Test
        @DisplayName("Steam login success - returns tokens")
        fun steamLoginSuccess() {
            val request = SteamLoginRequest(
                steamId = "76561198123456789",
                steamNickname = "TestPlayer"
            )
            
            val user = User.createSteamUser(request.steamId, request.steamNickname)

            every { oAuth2AuthService.processingSteamLogin(request.steamId, request.steamNickname) } returns user
            every { jwtTokenProvider.generateAccessToken(any(), any()) } returns "access-token"
            every { jwtTokenProvider.generateRefreshToken(any()) } returns "refresh-token"

            mockMvc.perform(
                post("/auth/oauth2/steam/callback")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.token").value("access-token"))
                .andExpect(jsonPath("$.refreshToken").value("refresh-token"))
                .andExpect(jsonPath("$.authProvider").value("STEAM"))

            verify { oAuth2AuthService.processingSteamLogin(request.steamId, request.steamNickname) }
        }
    }

    @Nested
    @DisplayName("OAuth Status")
    inner class GetOAuthStatus {

        @Test
        @DisplayName("OAuth user returns true")
        fun oauthUserReturnsTrue() {
            val userId = 1L
            every { oAuth2AuthService.isOAuthUser(userId) } returns true

            mockMvc.perform(
                get("/auth/oauth2/status")
                    .header("X-User-Id", userId)
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.isOAuthUser").value(true))
        }
    }
}
