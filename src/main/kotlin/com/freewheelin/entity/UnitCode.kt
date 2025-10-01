package com.freewheelin.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "unit_code")
data class UnitCode(
    @Id
    @Column(name = "code", length = 64)
    val code: String = "",

    @Column(name = "name", nullable = false, length = 255)
    val name: String = ""
)



