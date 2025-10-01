## 위험 요소
- 프로젝트 전반적으로 jpa 사용시 지연 로딩으로 설정된 연관 엔티티를 로직 중간에 조회하는 경우 추가 쿼리가 발생하여 N+1 문제가 생길 수 있음.
  - 적절한 fetch join 을 활용 및 일괄조회 하도록 하여 조치
  
- uni_code를 단순 문자열로 사용하면 타입 안전성이 떨어지고, enum class로 하기에는 값이 많고 변경·추가가 잦을 가능성이 있어 유지 보수가 어려움.
  - value class를 사용하고 엔드 포인트단 유효성 검사 설정을 통해 조치

## 성능 측면
- 문제 조회 API 사용시 문제 수가 많을 때 조회 데이터가 커져 서버 메모리 및 네트워크 부하가 증가.
  - DB 조회시 최근 기간에 대한 필터를 적용하고 id만을 레벨별 분할 조회하여 서버 메모리 및 네트워크 부하를 줄이고 난이도에 따라 샘플링.
 
## 고민거리
- 문제 조회 API의 요구사항대로 구현하면 같은 유형코드로 여러번 조회 시 항상 동일한 문제 목록만 반환되는 부분
  - 만약 선생님이 같은 유형으로 다양한 학습지를 만들려고 한다면 매번 똑같은 문제만 받게 되어 랜덤/로테이션 등 변동성이 없으면 기능의 본래 목적을 달성할 수 없는 것으로 보이는데 의도된 것인지 고민.

## [ERD](https://dbdiagram.io/d/프리윌린-과제-ERD-68d76b7bd2b621e422263160)

<img width="1064" height="884" alt="프리윌린 과제 ERD (1)" src="https://github.com/user-attachments/assets/9705f9d2-73dd-4290-9368-a9081c397bac" />


## API 명세

## 문제 조회
### Request
```http
url: http://localhost:8080/v1/problems?totalCount=5&unitCodeList=UC9999,UC9998&level=HIGH&problemType=SELECTION
methods: GET
```

### Response
```http
code: 200
```
```jsonc
{
  "problemList":[
    {"id":517,"answer":"2","unitCode":"UC9998","level":1,"problemType":"SELECTION"},
    {"id":673,"answer":"2","unitCode":"UC9998","level":5,"problemType":"SELECTION"}
   ]
}
```

## 학습지 생성
### Request
```http
url: http://localhost:8080/v1/piece?teacherUserId=1
methods: POST
Content-type: application/json
```
```jsonc
{
  "name": "중간평가 A반",
  "problems": [
    { "id": 101, "unitCode": "UC1503", "level": 1 },
    { "id": 102, "unitCode": "UC1503", "level": 2 }
    // ... 최대 50개
  ]
}
```

### Response
```http
code: 201
```
```jsonc
{
  "id": 10,
  "name": "중간평가 A반",
  "teacherUserId": 1,
  "teacherName": "김선생",
  "problemCount": 2,
  "createdAt": "2025-10-01T12:34:56.000"
}
```

## 학습지 문제 순서 수정
### Request
```http
url: http://localhost:8080/v1/piece/{pieceId}/order?teacherUserId=1
methods: PATCH
Content-type: application/json
```
```jsonc
{ "orderedProblemIds": [102, 101, 103] }
```

### Response
```http
code: 204
```

## 학생에게 학습지 출제
### Request
```http
url: http://localhost:8080/v1/piece/{pieceId}?teacherUserId=1&studentIds=2,3
methods: POST
```

### Response
```http
code: 200
```
```jsonc
[
  { "pieceId": 10, "pieceName": "중간평가 A반", "studentUserId": 2, "studentName": "김학생", "assignedAt": "2025-10-01T12:35:10.000" },
  { "pieceId": 10, "pieceName": "중간평가 A반", "studentUserId": 3, "studentName": "이학생", "assignedAt": "2025-10-01T12:35:10.010" }
]
```

## 학습지의 문제 조회(학생)
### Request
```http
url: http://localhost:8080/v1/piece/{pieceId}/problems?studentUserId=2
methods: GET
```

### Response
```http
code: 200
```
```jsonc
{
  "pieceId": 10,
  "pieceName": "중간평가 A반",
  "problems": [
    { "id": 101, "unitCode": "UC1503", "level": 1, "problemType": "SELECTION", "orderIndex": 1000 },
    { "id": 102, "unitCode": "UC1503", "level": 2, "problemType": "SUBJECTIVE", "orderIndex": 2000 }
    // ...
  ]
}
```

## 채점하기(학생)
### Request
```http
url: http://localhost:8080/v1/piece/{pieceId}/score?studentUserId=2
methods: PUT
Content-type: application/json
```
```jsonc
{
  "answers": [
    { "problemId": 101, "problemType": "SELECTION", "studentAnswer": "3" }
  ]
}
```

### Response
```http
code: 200
```
```jsonc
{
  "results": [
    { "pieceId": 10, "studentUserId": 2, "problemId": 101, "studentAnswer": "3", "correct": true, "scoredAt": "2025-10-01T12:36:00.000" }
  ]
}
```

## 학습지 학습 통계 분석(선생님)
### Request
```http
url: http://localhost:8080/v1/piece/{pieceId}/analyze?teacherUserId=1
methods: GET
```

### Response
```http
code: 200
```
```jsonc
{
  "pieceId": 10,
  "pieceName": "중간평가 A반",
  "assignedProblemCount": 15,
  "assignedStudents": [
    { "studentUserId": 2, "studentName": "김학생", "correctRate": 0.67, "answeredCount": 12, "correctCount": 8 }
  ],
  "problemAnalysis": [
    { "problemId": 101, "unitCode": "UC1503", "level": 1, "correctRate": 0.5, "totalAttempts": 10, "correctCount": 5 }
  ]
}
```

## Error (공통 예시)
```http
code: 400
```
```json
{ "message": "요청이 유효하지 않습니다", "timestamp": 1710000000000 }
```

