package com.freewheelin.entity

import jakarta.persistence.*
import java.io.Serializable
import java.time.LocalDateTime

@Entity
@Table(name = "answer")
@IdClass(AnswerId::class)
data class Answer(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "piece_id", nullable = false)
    val piece: Piece,
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_user_id", nullable = false)
    val student: User,
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    val problem: Problem,
    
    @Column(name = "answer_text", length = 1024)
    var answerText: String?,
    
    @Column(name = "correct")
    var correct: Boolean?,
    
    @Column(name = "scored_at")
    var scoredAt: LocalDateTime = LocalDateTime.now()
) {
    constructor() : this(Piece(), User(), Problem(), null, null, LocalDateTime.now())
}

data class AnswerId(
    val piece: Long? = null,
    val student: Long? = null,
    val problem: Long? = null
) : Serializable
