package com.freewheelin.repository

import com.freewheelin.entity.Problem
import com.freewheelin.model.ProblemType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface ProblemRepository : JpaRepository<Problem, Long> {

    @Query(
        """
        SELECT p.id FROM Problem p
        WHERE p.unitCode.code IN :unitCodes
          AND p.problemType IN :problemTypes
          AND p.level IN :levels
          AND p.createdAt >= :cutoff
        """
    )
    fun findIdsByFilters(
        @Param("unitCodes") unitCodes: List<String>,
        @Param("problemTypes") problemTypes: List<ProblemType>,
        @Param("levels") levels: List<Int>,
        @Param("cutoff") cutoff: java.time.LocalDateTime
    ): List<Long>
}
