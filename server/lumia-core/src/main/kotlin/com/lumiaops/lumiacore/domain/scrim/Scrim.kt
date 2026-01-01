package com.myeongho.lumia.core.domain.scrim

import com.myeongho.lumia.core.domain.common.BaseTimeEntity
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "scrims")
class Scrim(
    @Column(nullable = false)
    var title: String,

    @Column(nullable = false)
    var startTime: LocalDateTime,

    var isFinished: Boolean = false
) : BaseTimeEntity() {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null
}