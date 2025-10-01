package com.freewheelin.repository

import com.freewheelin.entity.Piece
import com.freewheelin.entity.PieceStudent
import com.freewheelin.entity.PieceStudentId
import com.freewheelin.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PieceStudentRepository : JpaRepository<PieceStudent, PieceStudentId> {
    fun findByPieceAndStudent(piece: Piece, student: User): PieceStudent?
    fun findByStudent(student: User): List<PieceStudent>
    fun findByPiece(piece: Piece): List<PieceStudent>
    
    @Query("SELECT ps FROM PieceStudent ps WHERE ps.piece = :piece AND ps.student IN :students")
    fun findByPieceAndStudentIn(@Param("piece") piece: Piece, @Param("students") students: List<User>): List<PieceStudent>
}
