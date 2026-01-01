package com.lumiaops.lumiacore.domain

import com.lumiaops.lumiacore.domain.common.BaseTimeEntity
import jakarta.persistence.*

@Entity
@Table(name = "users")
class User(
    @Column(nullable = false, unique = true)
    val email: String,

    @Column(nullable = false)
    var nickname: String,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val role: UserRole = UserRole.USER // USER, ADMIN
) : BaseTimeEntity() {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    // 비즈니스 메서드
    fun updateNickname(newNickname: String) {
        this.nickname = newNickname
    }
}

enum class UserRole { USER, ADMIN }