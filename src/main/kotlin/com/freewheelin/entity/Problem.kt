package com.freewheelin.entity

import com.freewheelin.model.ProblemType
import javax.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "problem")
data class Problem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_code", nullable = false, referencedColumnName = "code")
    val unitCode: UnitCode,

    @Column(nullable = false)
    val level: Int,
    
    @Enumerated(EnumType.STRING)
    @Column(name = "problem_type", nullable = false, length = 16)
    val problemType: ProblemType,
    
    @Column(length = 1024)
    val answer: String?,

    @Column(name = "created_at", nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now()
) {
    constructor() : this(null, UnitCode("UC1503", ""), 1, ProblemType.SELECTION, null, LocalDateTime.now())
}
