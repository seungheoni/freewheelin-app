package com.freewheelin.entity

import jakarta.persistence.*
import java.io.Serializable

@Entity
@Table(name = "piece_problem")
@IdClass(PieceProblemId::class)
data class PieceProblem(
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "piece_id", nullable = false)
    val piece: Piece,
    
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    val problem: Problem,
    
    @Column(name = "order_no")
    var orderNo: Int?
) {
    constructor() : this(Piece(), Problem(), null)
}

data class PieceProblemId(
    val piece: Long? = null,
    val problem: Long? = null
) : Serializable
