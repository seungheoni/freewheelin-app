package com.freewheelin.exception

enum class ErrorMessage(val defaultMessage: String) {
    // 공통 조회/권한
    TEACHER_NOT_FOUND("선생님을 찾을 수 없습니다: %s"),
    STUDENT_NOT_FOUND("학생을 찾을 수 없습니다: %s"),
    PIECE_NOT_FOUND("학습지를 찾을 수 없습니다: %s"),
    PROBLEM_NOT_FOUND("문제를 찾을 수 없습니다: %s"),
    TEACHER_ONLY_CREATE("선생님만 학습지를 생성할 수 있습니다"),
    TEACHER_ONLY_UPDATE_ORDER("선생님만 학습지 순서를 수정할 수 있습니다"),
    TEACHER_ONLY_ASSIGN("선생님만 학습지를 출제할 수 있습니다"),
    STUDENT_ONLY_SCORE("학생만 채점할 수 있습니다"),

    // 학습지/문항 구성
    PIECE_PROBLEM_INVALID_ID("학습지에 존재하지 않는 problemId 입니다: %s"),
    PIECE_PROBLEM_NOT_IN_PIECE("해당 학습지의 문제가 아닙니다: problemId=%s"),
    PIECE_MAX_PROBLEMS_EXCEEDED("학습지에는 최대 50개의 문제만 포함할 수 있습니다"),
    PROBLEM_IDS_MISSING("존재하지 않는 문제 ID가 포함되어 있습니다: %s"),

    // 배정/검증
    PIECE_NOT_ASSIGNED_TO_USER("해당 user에게 출제되지 않은 학습지입니다"),
    TYPE_MISMATCH("요청 problemType이 실제 문제 유형과 일치하지 않습니다: problemId=%s"),
    TYPE_ALL_NOT_ALLOWED("채점 요청에 사용할 수 없는 problemType입니다: ALL"),
    SELECTION_ANSWER_INVALID("객관식 답변은 1~4 숫자여야 합니다: problemId=%s"),

    // 파라미터 범위
    PROBLEM_IDS_SET_MISMATCH("problemIds가 현재 학습지 문제 집합과 일치하지 않습니다");

    fun format(vararg args: Any?): String = defaultMessage.format(*args)
}


