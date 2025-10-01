package com.freewheelin.repository

import com.freewheelin.entity.Answer
import com.freewheelin.entity.AnswerId
import com.freewheelin.entity.Piece
import com.freewheelin.entity.Problem
import com.freewheelin.entity.User
import com.freewheelin.dto.StudentStat
import com.freewheelin.dto.ProblemStat
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface AnswerRepository : JpaRepository<Answer, AnswerId> {
    @Query("SELECT a FROM Answer a WHERE a.piece = :piece AND a.student = :student AND a.problem.id IN :problemIds")
    fun findByPieceAndStudentAndProblemIdIn(
        @Param("piece") piece: Piece,
        @Param("student") student: User,
        @Param("problemIds") problemIds: Set<Long>
    ): List<Answer>

    @Query(
        """
        SELECT new com.freewheelin.dto.StudentStat(
            a.student.id, a.student.name,
            SUM(CASE WHEN a.correct = true THEN 1 ELSE 0 END),
            COUNT(a)
        )
        FROM Answer a
        WHERE a.piece = :piece
        GROUP BY a.student.id, a.student.name
        """
    )
    fun findStudentStatsByPiece(@Param("piece") piece: Piece): List<StudentStat>

    @Query(
        """
        SELECT new com.freewheelin.dto.ProblemStat(
            a.problem.id,
            SUM(CASE WHEN a.correct = true THEN 1 ELSE 0 END),
            COUNT(a)
        )
        FROM Answer a
        WHERE a.piece = :piece
        GROUP BY a.problem.id
        """
    )
    fun findProblemStatsByPiece(@Param("piece") piece: Piece): List<ProblemStat>
}
