package com.freewheelin.dto
import com.freewheelin.model.Level
import com.freewheelin.model.UnitCode
import com.freewheelin.model.ProblemType
import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import javax.validation.constraints.*
import javax.validation.Valid


@Schema(description = "문제 검색 요청")
data class ProblemSearchRequest(
    @Schema(description = "총 문제 수", example = "15")
    @field:Min(1)
    @field:Max(50)
    val totalCount: Int,
    
    @ArraySchema(schema = Schema(type = "string", example = "UC9999"))
    @field:NotEmpty val unitCodeList: List<UnitCode>,
    
    @Schema(description = "난이도", example = "MIDDLE", allowableValues = ["LOW", "MIDDLE", "HIGH"])
    @field:NotNull
    val level: Level,
    
    @Schema(description = "문제 유형", example = "ALL", allowableValues = ["ALL", "SUBJECTIVE", "SELECTION"])
    @field:NotNull
    val problemType: ProblemType
)

@Schema(description = "문제 응답")
data class ProblemResponse(
    @Schema(description = "문제 ID", example = "1")
    val id: Long,
    
    @Schema(description = "정답", example = "5")
    val answer: String,
    
    @Schema(description = "유형코드", example = "UC1503")
    val unitCode: String,
    
    @Schema(description = "난이도 (1-5)", example = "1")
    val level: Int,
    
    @Schema(description = "문제 유형", example = "SELECTION")
    val problemType: String
)

@Schema(description = "문제 목록 응답")
data class ProblemListResponse(
    @Schema(description = "문제 목록")
    val problemList: List<ProblemResponse>
)

