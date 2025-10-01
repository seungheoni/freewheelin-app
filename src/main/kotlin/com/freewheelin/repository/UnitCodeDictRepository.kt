package com.freewheelin.repository

import com.freewheelin.entity.UnitCode
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface UnitCodeRepository : JpaRepository<UnitCode, String>



