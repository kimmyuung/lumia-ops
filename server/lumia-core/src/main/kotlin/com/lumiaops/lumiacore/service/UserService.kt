package com.lumiaops.lumiacore.service


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


    @Transactional
    fun createUser(email: String, nickname: String, role: UserRole = UserRole.USER): User {
        if (existsByEmail(email)) {
            throw IllegalArgumentException("이미 존재하는 이메일입니다: $email")
        }
        return userRepository.save(User(email = email, nickname = nickname, role = role))
    }


    @Transactional
    fun updateNickname(userId: Long, newNickname: String): User {
        val user = findById(userId) ?: throw IllegalArgumentException("사용자를 찾을 수 없습니다: $userId")
        user.updateNickname(newNickname)
        return user
    }


    @Transactional
    fun deleteUser(userId: Long) {
        userRepository.deleteById(userId)
    }
}
