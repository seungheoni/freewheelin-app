package com.freewheelin.entity

import javax.persistence.Column
import javax.persistence.Entity
import javax.persistence.Id
import javax.persistence.Table

@Entity
@Table(name = "unit_code")
data class UnitCode(
    @Id
    @Column(name = "code", length = 64)
    val code: String = "",

    @Column(name = "name", nullable = false, length = 255)
    val name: String = ""
)



