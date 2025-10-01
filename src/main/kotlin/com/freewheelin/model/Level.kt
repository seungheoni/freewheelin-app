package com.freewheelin.model

enum class Level(val displayName: String, val levels: List<Int>) {
    LOW("하", listOf(1)),
    MIDDLE("중", listOf(2, 3, 4)),
    HIGH("상", listOf(5));

    fun rates(): List<Double> = when (this) {
        LOW -> listOf(0.5, 0.3, 0.2)
        MIDDLE -> listOf(0.25, 0.5, 0.25)
        HIGH -> listOf(0.2, 0.3, 0.5)
    }

    companion object {
        fun fromString(level: String): Level? {
            return values().find { it.name.equals(level, ignoreCase = true) }
        }
    }
}


