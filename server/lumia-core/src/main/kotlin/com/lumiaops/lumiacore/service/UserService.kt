package com.lumiaops.lumiacore.service


import com.lumiaops.lumiacore.domain.AccountStatus
import com.lumiaops.lumiacore.domain.User
import com.lumiaops.lumiacore.domain.UserRole
import com.lumiaops.lumiacore.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository
) {
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
        return user
    }

    /**
     * 닉네임 변경 (30일 제한)
     */
    @Transactional
    fun updateNickname(userId: Long, newNickname: String): User {
        val user = findById(userId) 
            ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다: $userId")
        
        user.updateNickname(newNickname)
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
        userRepository.deleteById(userId)
    }
}
