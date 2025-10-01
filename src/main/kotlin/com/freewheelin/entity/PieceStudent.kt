package com.freewheelin.entity

import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "piece_student")
@IdClass(PieceStudentId::class)
data class PieceStudent(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "piece_id", nullable = false)
    val piece: Piece,
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_user_id", nullable = false)
    val student: User
) {
    constructor() : this(Piece(), User())
}

data class PieceStudentId(
    val piece: Long? = null,
    val student: Long? = null
) : Serializable
