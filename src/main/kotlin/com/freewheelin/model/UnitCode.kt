package com.freewheelin.model

import jakarta.persistence.AttributeConverter
import jakarta.persistence.Converter
import com.freewheelin.exception.ErrorMessage
import io.swagger.v3.oas.annotations.media.Schema

@Schema(type = "string", example = "UC9999", description = "유형코드(대문자)")
@JvmInline
value class UnitCode private constructor(val value: String) {
    companion object {
        private val pattern = Regex("(?i)^uc\\d{4}$")
        fun of(raw: String): UnitCode {
            val trimmed = raw.trim()
            require(pattern.matches(trimmed)) { ErrorMessage.PROBLEM_IDS_MISSING.defaultMessage }
            return UnitCode(trimmed.uppercase())
        }
    }
    override fun toString(): String = value
}

@Converter(autoApply = true)
class UnitCodeConverter : AttributeConverter<UnitCode, String> {
    override fun convertToDatabaseColumn(attribute: UnitCode?): String? = attribute?.value
    override fun convertToEntityAttribute(dbData: String?): UnitCode? = dbData?.let { UnitCode.of(it) }
}


