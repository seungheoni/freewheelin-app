package com.freewheelin.runner

import com.freewheelin.entity.*
import com.freewheelin.model.ProblemType
import com.freewheelin.model.UnitCode as ModelUnitCode
import com.freewheelin.repository.*
import org.apache.poi.ss.usermodel.Cell
import org.apache.poi.ss.usermodel.CellType
import org.apache.poi.ss.usermodel.Row
import org.apache.poi.ss.usermodel.Workbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.event.EventListener
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.io.File
import java.io.FileInputStream
import kotlin.random.Random

@Component
@Profile("dev")
class AppDevDataInitRunner(
    private val userRepository: UserRepository,
    private val problemRepository: ProblemRepository,
    private val unitCodeRepository: UnitCodeRepository,
    @Value("\${app.data.excel.file-path:data/backend_recruit_data.xlsx}")
    private val excelFilePath: String
) : CommandLineRunner {

    private val unitCodeDisplayNames: Map<String, String> = mapOf(
        "UC1572" to "모수와 통계량 구별, 모집단과 표본",
        "UC1573" to "도수분포표, 히스토그램, 도수분포다각형 작성 및 해석",
        "UC1574" to "줄기-잎 그림 작성 및 해석",
        "UC1575" to "막대그래프, 원 그래프",
        "UC1576" to "중심 경향 척도 : 자료의 평균, 평균절대편차, 최빈값",
        "UC1577" to "산포도 : 자료의 범위, 사분위수, IQR",
        "UC1578" to "산포도 : 분산, 표준편차",
        "UC1579" to "다변량 자료, 분할표, 산점도, 공분산과 상관계수",
        "UC1503" to "합집합, 교집합 및 여집합",
        "UC1506" to "사건의 합집합, 교집합 및 여집합의 계산",
        "UC1510" to "기본 조건부 확률의 계산",
        "UC1513" to "세 개 이상 집합의 교집합",
        "UC1519" to "순열과 조합을 이용한 확률 계산",
        "UC1520" to "이산확률분포, 이산확률변수, 확률질량함수",
        "UC1521" to "연속확률분포, 연속확률변수, 확률밀도함수",
        "UC1523" to "확률변수의 기대값",
        "UC1524" to "확률변수의 분산, 표준편차",
        "UC1570" to "확률변수의 공분산과 상관계수",
        "UC1526" to "결합확률밀도함수",
        "UC1529" to "확률변수의 독립과 종속 여부 판단",
        "UC1534" to "이항분포 기본 계산, 이항분포의 평균 및 표준 편차",
        "UC1535" to "이항 공식, 이항분포를 사용하여 정확히 m번 성공할 확률 찾기",
        "UC1536" to "이항 공식, 이항분포를 사용하여 m번 이상 또는 미만 성공할 확률 찾기",
        "UC1537" to "비복원추출, 초기하분포, 초기하확률",
        "UC1539" to "정규분포, 정규분포의 확률밀도함수, 정규분포의 확률 계산",
        "UC1540" to "표준정규분포의 확률 계산하기",
        "UC1541" to "중심극한정리 : 표본 평균의 표본 분포",
        "UC1542" to "중심극한정리 : 이항 분포에 대한 정규 근사",
        "UC1548" to "모평균에 대한 신뢰구간",
        "UC1564" to "F분포",
        "UC1568" to "카이제곱분포, 자유도, 표본분산, 카이제곱 검정",
        "UC1580" to "표본추출 : 단순랜덤추출",
        "UC1581" to "표본추출 : 계통추출법",
        "UC1582" to "표본추출 : 표집틀(sampling frame)",
        "UC1583" to "실험단위, 인자, 반응변수, 처리",
        "UC1584" to "랜덤화, 블록화, 랜덤블록계획, 반복",
        "UC1571" to "SAS 명령어, 함수",
        "UC9999" to "테스트 유형1",
        "UC9998" to "테스트 유형2"
    )

    @Transactional
    override fun run(vararg args: String?) {
        val excelFile = File(excelFilePath)

        if (excelFile.exists()) {
            println("=== 엑셀 파일에서 데이터 로드 시작: $excelFilePath ===")
            loadDataFromExcel(excelFilePath)
        } else {
            println("엑셀 파일이 없습니다: $excelFilePath")
            println("기본 데이터로 초기화합니다.")
            initializeDefaultData()
        }

        initializeUsers()

        seedUnitCodeDict()
        seedTestProblems()
    }

    @Transactional
    fun loadDataFromExcel(filePath: String) {
        FileInputStream(filePath).use { fis ->
            val workbook: Workbook = XSSFWorkbook(fis)
            try {
                println("\n사용 가능한 시트들:")
                for (i in 0 until workbook.numberOfSheets) {
                    val sheetName = workbook.getSheetName(i)
                    val sheet = workbook.getSheetAt(i)
                    println("- $sheetName (행 수: ${sheet.lastRowNum + 1})")
                }

                loadUsers(workbook)
                loadProblems(workbook)

                println("\n엑셀 데이터 로드 완료")
            } finally {
                workbook.close()
            }
        }
    }

    private fun loadUsers(workbook: Workbook) {
        val sheet = workbook.getSheet("Users") ?: workbook.getSheet("users") ?: return

        if (userRepository.count() > 0) {
            println("User 데이터가 이미 존재하여 Excel 로드를 건너뜁니다.")
            return
        }

        println("\nUser 시트 분석 중...")
        println("시트명: ${sheet.sheetName}")
        println("행 수: ${sheet.lastRowNum + 1}")

        for (i in 0..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue

            if (i == 0) {
                println("헤더: ${getRowValues(row)}")
                continue
            }

            val email = getCellValueAsString(row.getCell(0))?.trim() ?: continue
            val name = getCellValueAsString(row.getCell(1))?.trim() ?: continue
            val roleString = getCellValueAsString(row.getCell(2))?.trim() ?: continue

            if (email.isNotEmpty() && name.isNotEmpty() && roleString.isNotEmpty()) {
                val role = try {
                    UserRole.valueOf(roleString.uppercase())
                } catch (_: IllegalArgumentException) {
                    UserRole.STUDENT
                }
                userRepository.save(User(email = email, name = name, role = role))
                println("User 추가: $email -> $name ($role)")
            }
        }
        println("User 데이터 ${userRepository.count()}개 로드 완료.")
    }

    private fun loadProblems(workbook: Workbook) {
        val sheet = workbook.getSheet("Problems")
            ?: workbook.getSheet("problems")
            ?: workbook.getSheet("problem")
            ?: return

        problemRepository.deleteAll()
        println("기존 Problem 데이터 삭제 완료")

        println("\nProblem 시트 분석 중...")
        println("시트명: ${sheet.sheetName}")
        println("행 수: ${sheet.lastRowNum + 1}")

        for (i in 0..sheet.lastRowNum) {
            val row = sheet.getRow(i) ?: continue

            if (i == 0) {
                println("헤더: ${getRowValues(row)}")
                continue
            }

            val unitCodeString = getCellValueAsString(row.getCell(1))?.trim() ?: continue
            val unitCode = try {
                ModelUnitCode.of(unitCodeString)
            } catch (_: IllegalArgumentException) {
                println("알 수 없는 UnitCode: $unitCodeString. 기본값 uc1503으로 설정합니다.")
                ModelUnitCode.of("uc1503")
            }

            val level = row.getCell(2)?.let { cell ->
                when (cell.cellType) {
                    CellType.NUMERIC -> cell.numericCellValue.toInt()
                    CellType.STRING -> cell.stringCellValue.toIntOrNull() ?: 1
                    else -> 1
                }
            } ?: 1

            val problemTypeString = getCellValueAsString(row.getCell(3))?.trim() ?: continue
            val problemType = try {
                ProblemType.valueOf(problemTypeString.uppercase())
            } catch (_: IllegalArgumentException) {
                ProblemType.SELECTION
            }

            val answer = row.getCell(4)?.let { cell ->
                when (cell.cellType) {
                    CellType.STRING -> cell.stringCellValue
                    CellType.NUMERIC -> cell.numericCellValue.toString()
                    CellType.BOOLEAN -> cell.booleanCellValue.toString()
                    else -> null
                }
            }

            if (isValidProblem(unitCodeString, level, problemTypeString)) {
                val unitDict: com.freewheelin.entity.UnitCode = getOrCreateUnit(unitCode.value)
                problemRepository.save(
                    Problem(
                        unitCode = unitDict,
                        level = level,
                        problemType = problemType,
                        answer = answer
                    )
                )
            } else {
                println("유효하지 않은 Problem 데이터 스킵: $unitCodeString (레벨:$level, 타입:$problemTypeString)")
            }
        }
        println("Problem 데이터 ${problemRepository.count()}개 로드 완료.")
    }

    private fun seedUnitCodeDict() {
        val existingProblemCodes: Set<String> = problemRepository.findAll()
            .map { it.unitCode.code.uppercase() }
            .toSet()
        val defaults: Set<String> = setOf("UC9999", "UC9998")
        val mappedCodes: Set<String> = unitCodeDisplayNames.keys
        val codesToEnsure: Set<String> = existingProblemCodes + defaults + mappedCodes
        val dicts: List<com.freewheelin.entity.UnitCode> = codesToEnsure
            .sorted()
            .map { code -> com.freewheelin.entity.UnitCode(code, unitCodeDisplayNames[code] ?: code) }
        unitCodeRepository.saveAll(dicts)
        println("UnitCode 사전 데이터 ${unitCodeRepository.count()}개 생성 완료.")
    }

    private fun seedTestProblems() {
        val targetUnits: List<ModelUnitCode> = listOf(ModelUnitCode.of("uc9999"), ModelUnitCode.of("uc9998"))
        val types: List<ProblemType> = listOf(ProblemType.SELECTION, ProblemType.SUBJECTIVE)
        val bulk: MutableList<Problem> = mutableListOf()

        targetUnits.forEach { unitCode ->
            val existingTest: List<Problem> = problemRepository.findAll().filter { it.unitCode.code == unitCode.value }
            if (existingTest.isNotEmpty()) problemRepository.deleteAll(existingTest)

            for (level: Int in 1..5) {
                types.forEach { problemType ->
                    repeat(20) {
                        val numericAnswer: String = Random.nextInt(1, 5).toString()
                        val unitDict: com.freewheelin.entity.UnitCode = getOrCreateUnit(unitCode.value)
                        bulk.add(
                            Problem(
                                unitCode = unitDict,
                                level = level,
                                problemType = problemType,
                                answer = numericAnswer
                            )
                        )
                    }
                }
            }
        }
        if (bulk.isNotEmpty()) {
            problemRepository.saveAll(bulk)
            println("테스트 Problem 데이터 ${bulk.size}개 추가 완료.")
        }
    }

    private fun initializeDefaultData() {
        initializeUsers()
        initializeProblems()
    }

    private fun initializeUsers() {
        if (userRepository.count() == 0L) {
            val teacher = User(
                email = "teacher1@example.com",
                name = "김선생",
                role = UserRole.TEACHER
            )
            val student1 = User(
                email = "student1@example.com",
                name = "김학생",
                role = UserRole.STUDENT
            )
            val student2 = User(
                email = "student2@example.com",
                name = "이학생",
                role = UserRole.STUDENT
            )
            userRepository.saveAll(listOf(teacher, student1, student2))
            println("기본 User 데이터 ${userRepository.count()}개 생성 완료.")
        }
    }

    private fun initializeProblems() {
        if (problemRepository.count() == 0L) {
            val problems = listOf(
                Problem(unitCode = getOrCreateUnit("UC1572"), level = 1, problemType = ProblemType.SELECTION, answer = "모수"),
                Problem(unitCode = getOrCreateUnit("UC1573"), level = 2, problemType = ProblemType.SUBJECTIVE, answer = "히스토그램"),
                Problem(unitCode = getOrCreateUnit("UC1576"), level = 2, problemType = ProblemType.SELECTION, answer = "평균"),
                Problem(unitCode = getOrCreateUnit("UC1578"), level = 3, problemType = ProblemType.SUBJECTIVE, answer = "표준편차"),
                Problem(unitCode = getOrCreateUnit("UC1503"), level = 1, problemType = ProblemType.SELECTION, answer = "합집합"),
                Problem(unitCode = getOrCreateUnit("UC1510"), level = 2, problemType = ProblemType.SUBJECTIVE, answer = "조건부확률"),
                Problem(unitCode = getOrCreateUnit("UC1520"), level = 3, problemType = ProblemType.SUBJECTIVE, answer = "이산확률분포"),
                Problem(unitCode = getOrCreateUnit("UC1521"), level = 3, problemType = ProblemType.SUBJECTIVE, answer = "연속확률분포"),
                Problem(unitCode = getOrCreateUnit("UC1534"), level = 4, problemType = ProblemType.SUBJECTIVE, answer = "이항분포"),
                Problem(unitCode = getOrCreateUnit("UC1539"), level = 4, problemType = ProblemType.SUBJECTIVE, answer = "정규분포"),
                Problem(unitCode = getOrCreateUnit("UC1540"), level = 5, problemType = ProblemType.SUBJECTIVE, answer = "표준정규분포"),
                Problem(unitCode = getOrCreateUnit("UC1541"), level = 5, problemType = ProblemType.SUBJECTIVE, answer = "중심극한정리"),
                Problem(unitCode = getOrCreateUnit("UC1564"), level = 4, problemType = ProblemType.SUBJECTIVE, answer = "F분포"),
                Problem(unitCode = getOrCreateUnit("UC1568"), level = 4, problemType = ProblemType.SUBJECTIVE, answer = "카이제곱분포"),
                Problem(unitCode = getOrCreateUnit("UC1571"), level = 3, problemType = ProblemType.SUBJECTIVE, answer = "SAS 함수")
            )
            problemRepository.saveAll(problems)
            println("기본 Problem 데이터 ${problemRepository.count()}개 생성 완료.")
        }
    }

    private fun getCellValueAsString(cell: Cell?): String? {
        if (cell == null) return null
        return when (cell.cellType) {
            CellType.STRING -> cell.stringCellValue
            CellType.NUMERIC -> cell.numericCellValue.toString()
            CellType.BOOLEAN -> cell.booleanCellValue.toString()
            CellType.FORMULA -> try { cell.stringCellValue } catch (_: Exception) { cell.numericCellValue.toString() }
            else -> ""
        }
    }

    private fun getRowValues(row: Row): List<String> {
        val lastIndex = row.lastCellNum
        val values = ArrayList<String>(lastIndex.toInt().coerceAtLeast(0))
        for (index in 0 until lastIndex) {
            val cell = row.getCell(index)
            values.add(getCellValueAsString(cell) ?: "")
        }
        return values
    }

    private fun isValidProblem(unitCode: String, level: Int, problemType: String): Boolean {
        val invalidUnitCodes = setOf("index", "unit_code", "유형코드", "code", "id", "번호")
        val invalidProblemTypes = setOf("problem_type", "타입", "type", "유형")
        return unitCode.isNotEmpty() &&
            !invalidUnitCodes.contains(unitCode.lowercase()) &&
            !invalidProblemTypes.contains(problemType.lowercase()) &&
            level in 1..5 &&
            !unitCode.matches(Regex("\\d+"))
    }

    private fun getOrCreateUnit(code: String): com.freewheelin.entity.UnitCode {
        val upper = code.uppercase()
        return unitCodeRepository.findById(upper).orElseGet {
            unitCodeRepository.save(com.freewheelin.entity.UnitCode(upper, upper))
        }
    }

    @EventListener(ApplicationReadyEvent::class)
    fun createSamplePieceAfterStartup() {
        try {
            val rest = org.springframework.web.client.RestTemplate()
            val problemsUrl = "http://localhost:8080/v1/problems" +
                "?totalCount=15&unitCodeList=UC9999&unitCodeList=UC9998&level=MIDDLE&problemType=ALL"

            val problemsResp = rest.getForEntity(
                problemsUrl,
                com.freewheelin.dto.ProblemListResponse::class.java
            )
            val problemList = problemsResp.body?.problemList ?: emptyList()
            if (problemList.isEmpty()) {
                println("자동 생성용 문제 조회 결과가 비어있어 학습지 생성 생략")
                return
            }

            val selected = problemList.map {
                com.freewheelin.dto.SelectedProblemRequest(
                    id = it.id,
                    unitCode = it.unitCode,
                    level = it.level
                )
            }
            val pieceReq = com.freewheelin.dto.PieceCreateRequest(
                name = "AUTO MIDDLE UC9999+UC9998",
                problems = selected
            )

            val pieceResp = rest.postForEntity(
                "http://localhost:8080/v1/piece?teacherUserId=1",
                pieceReq,
                com.freewheelin.dto.PieceResponse::class.java
            )
            val created = pieceResp.body
            if (created != null) {
                println("자동 학습지 생성 완료: id=${created.id}, name=${created.name}, problemCount=${created.problemCount}")
            }
        } catch (e: Exception) {
            println("자동 학습지 생성 중 예외 발생: ${e.message}")
        }
    }
}