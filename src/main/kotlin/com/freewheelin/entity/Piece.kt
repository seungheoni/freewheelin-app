package com.freewheelin.entity

import javax.persistence.*

@Entity
@Table(name = "piece")
data class Piece(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, length = 255)
    val name: String,
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_user_id", nullable = false)
    val creator: User,
    
    @OneToMany(mappedBy = "piece", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val pieceProblems: MutableList<PieceProblem> = mutableListOf(),
    
    @OneToMany(mappedBy = "piece", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val pieceStudents: MutableList<PieceStudent> = mutableListOf(),
    
    @OneToMany(mappedBy = "piece", cascade = [CascadeType.ALL], fetch = FetchType.LAZY)
    val answers: MutableList<Answer> = mutableListOf()
) {
    constructor() : this(null, "", User(), mutableListOf(), mutableListOf(), mutableListOf())
}
