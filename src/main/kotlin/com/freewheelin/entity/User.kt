package com.freewheelin.entity

import javax.persistence.*
import java.time.LocalDateTime

enum class UserRole {
    TEACHER, STUDENT
}

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    
    @Column(nullable = false, unique = true)
    val email: String,
    
    @Column(nullable = false, length = 100)
    val name: String,
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 16)
    val role: UserRole,
    
    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),
    
    @Column(name = "updated_at")
    val updatedAt: LocalDateTime = LocalDateTime.now()
) {
    constructor() : this(null, "", "", UserRole.STUDENT, LocalDateTime.now(), LocalDateTime.now())
}

