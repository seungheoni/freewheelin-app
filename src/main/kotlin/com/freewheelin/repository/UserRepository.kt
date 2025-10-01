package com.freewheelin.repository

import com.freewheelin.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String): Optional<User>
    fun findByNameContainingIgnoreCase(name: String): List<User>
}

