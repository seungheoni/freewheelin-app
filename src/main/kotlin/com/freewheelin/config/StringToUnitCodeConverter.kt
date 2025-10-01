package com.freewheelin.config

import com.freewheelin.model.UnitCode
import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class StringToUnitCodeConverter : Converter<String, UnitCode> {
    override fun convert(source: String): UnitCode = UnitCode.of(source)
}


