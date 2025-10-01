package com.freewheelin.repository

import com.freewheelin.entity.Piece
import com.freewheelin.entity.PieceProblem
import com.freewheelin.entity.PieceProblemId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface PieceProblemRepository : JpaRepository<PieceProblem, PieceProblemId> {
    // Deprecated: prefer fetch-join version below

    @Query("SELECT COUNT(pp) FROM PieceProblem pp WHERE pp.piece = :piece AND pp.problem.id = :problemId")
    fun countByPieceAndProblemId(@Param("piece") piece: Piece, @Param("problemId") problemId: Long): Long

    @Query("""
        SELECT pp FROM PieceProblem pp
        JOIN FETCH pp.problem p
        WHERE pp.piece = :piece
        ORDER BY pp.orderNo
    """)
    fun findByPieceWithProblemOrderByOrderNo(@Param("piece") piece: Piece): List<PieceProblem>

    @Query("SELECT pp.problem.id FROM PieceProblem pp WHERE pp.piece = :piece")
    fun findProblemIdsByPiece(@Param("piece") piece: Piece): List<Long>
}
