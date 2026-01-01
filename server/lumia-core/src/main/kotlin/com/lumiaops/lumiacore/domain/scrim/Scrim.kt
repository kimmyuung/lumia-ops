package com.lumiaops.lumiacore.domain.scrim

import com.lumiaops.lumiacore.domain.common.BaseTimeEntity
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