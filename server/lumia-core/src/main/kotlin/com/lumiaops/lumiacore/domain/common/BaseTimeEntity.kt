package com.myeongho.lumia.core.domain.common

import jakarta.persistence.EntityListeners
import jakarta.persistence.MappedSuperclass
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@MappedSuperclass // 1. 이 클래스는 테이블로 생성되지 않고, 자식 엔티티에게 컬럼만 물려줍니다.
@EntityListeners(AuditingEntityListener::class) // 2. JPA Auditing 기능을 사용하여 시간을 자동 주입합니다.
abstract class BaseTimeEntity {

    @CreatedDate
    var createdAt: LocalDateTime = LocalDateTime.now()
        protected set // 외부에서 임의로 수정하지 못하게 막습니다.

    @LastModifiedDate
    var updatedAt: LocalDateTime = LocalDateTime.now()
        protected set
}