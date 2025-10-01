package com.freewheelin.repository

import com.freewheelin.entity.Piece
import com.freewheelin.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PieceRepository : JpaRepository<Piece, Long> {
    
    @Query("SELECT p FROM Piece p WHERE p.creator = :creator AND p.id = :pieceId")
    fun findByCreatorAndId(@Param("creator") creator: User, @Param("pieceId") pieceId: Long): Piece?
}
