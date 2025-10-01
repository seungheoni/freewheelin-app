package com.freewheelin.model

enum class ProblemType(val displayName: String) {
    ALL("전체"),
    SUBJECTIVE("주관식"),
    SELECTION("객관식");

    companion object {
        fun fromString(type: String): ProblemType? {
            return values().find { it.name.equals(type, ignoreCase = true) }
        }
    }
}


