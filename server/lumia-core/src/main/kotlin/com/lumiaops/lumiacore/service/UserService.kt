package com.lumiaops.lumiacore.service


import com.lumiaops.lumiacore.domain.AccountStatus
import com.lumiaops.lumiacore.domain.User
import com.lumiaops.lumiacore.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository
) {
    private val log = LoggerFactory.getLogger(javaClass)

    fun findById(id: Long): User? = userRepository.findById(id).orElse(null)

    fun findByEmail(email: String): User? = userRepository.findByEmail(email)

    fun existsByEmail(email: String): Boolean = userRepository.existsByEmail(email)

    fun findAll(): List<User> = userRepository.findAll()

    /**
     * 닉네임 설정 (첫 설정 - 이메일 인증 후)
     */
    @Transactional
    fun setInitialNickname(userId: Long, nickname: String): User {
        val user = findById(userId) 
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다: $userId")
        
        if (user.status != AccountStatus.PENDING_NICKNAME) {
            throw IllegalArgumentException("닉네임 설정 대기 상태가 아닙니다")
        }
        
        user.setInitialNickname(nickname)
        log.info("닉네임 초기 설정: userId=$userId, nickname=$nickname")
        return user
    }

    /**
     * 닉네임 변경 (30일 제한)
     */
    @Transactional
    fun updateNickname(userId: Long, newNickname: String): User {
        val user = findById(userId) 
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다: $userId")
        
        val oldNickname = user.nickname
        user.updateNickname(newNickname)
        log.info("닉네임 변경: userId=$userId, nickname: $oldNickname→$newNickname")
        return user
    }

    /**
     * 닉네임 변경까지 남은 일수 조회
     */
    fun getDaysUntilNicknameChange(userId: Long): Long {
        val user = findById(userId) 
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다: $userId")
        return user.daysUntilNicknameChange()
    }

    @Transactional
    fun deleteUser(userId: Long) {
        log.warn("사용자 삭제: userId=$userId")
        userRepository.deleteById(userId)
    }

    /**
     * 이터널 리턴 인게임 닉네임 업데이트
     */
    @Transactional
    fun updateGameNickname(userId: Long, gameNickname: String?): User {
        val user = findById(userId)
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다: $userId")
        
        user.updateGameNickname(gameNickname)
        log.info("게임 닉네임 업데이트: userId=$userId, gameNickname=$gameNickname")
        return user
    }
}
