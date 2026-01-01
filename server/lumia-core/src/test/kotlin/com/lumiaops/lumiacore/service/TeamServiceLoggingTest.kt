package com.lumiaops.lumiacore.service

import com.lumiaops.lumiacore.domain.Team
import com.lumiaops.lumiacore.repository.TeamRepository
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.boot.test.system.CapturedOutput
import org.springframework.boot.test.system.OutputCaptureExtension

@SpringBootTest // AOP가 동작하려면 스프링 컨테이너가 필요합니다.
@ExtendWith(OutputCaptureExtension::class) // 로그 출력을 캡처하는 도구입니다.
class TeamServiceLoggingTest {

    @Autowired
    private lateinit var teamService: TeamService

    @MockitoBean // 실제 DB까지 안 가고 흉내만 냅니다 (로그 테스트 목적이므로)
    private lateinit var teamRepository: TeamRepository

    @Test
    @DisplayName("createTeam 메서드 실행 시 START/END 로그가 찍혀야 한다")
    fun logExecutionTest(output: CapturedOutput) {
        // given
        val teamName = "LumiaTeam"
        val ownerId = 100L // 테스트용 ID 임의 지정
        val description = "test"

        // 리포지토리가 가짜(Mock)이므로 저장 시 동작을 정의해줍니다.
        given(teamRepository.save(org.mockito.ArgumentMatchers.any()))
            .willReturn(Team(name = teamName, ownerId = ownerId))

        // when
        teamService.createTeam(teamName, description, ownerId)

        // then (로그 확인)
        val logs = output.all

        // 1. 시작 로그 확인 - 실제 로그 형식: "👉 [START] TeamService.createTeam() Args: ..."
        assertThat(logs).contains("👉 [START] TeamService.createTeam()")
        assertThat(logs).contains("Args: $teamName")

        // 2. 종료 로그 확인 - 실제 로그 형식: "[END] TeamService.createTeam() - 소요시간:"
        assertThat(logs).contains("[END] TeamService.createTeam()")
        assertThat(logs).contains("소요시간:")
    }
}