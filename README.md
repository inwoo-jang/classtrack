# ClassTrack

수강 중인 강의와 과제를 한곳에서 추적하는 웹 애플리케이션.
Spring Boot 백엔드와 Vue 프론트엔드로 이루어져 있다.

## 스택

| | |
|---|---|
| 백엔드 | Java 21, Spring Boot 4.1, Spring Data JPA, AspectJ |
| DB | PostgreSQL (Neon) |
| 프론트엔드 | Vue 3, TypeScript, Vite |

## 기능

- **강의** 등록·수정·삭제, 과목별 분류, 대면/비대면 구분
- **과제** 등록·삭제, 진행 상태(진행 전/진행 중/완료), 결과물 링크(GitHub·Drive) 연결
- **캘린더** — 주말과 공휴일을 제외한 실제 수업일 표시
- **대시보드** — 강의·과제 집계와 진행 중인 항목
- **dev 화면** — 실행 중인 앱에서 뽑아낸 API·서비스·DB 구조와 실시간 로그

## 눈여겨볼 구현

**수업일 계산** — `durationDays` 는 달력 날짜가 아니라 수업일 수다. 종료일은 주말·공휴일을
건너뛰며 계산하므로 저장하지 않고 [`AcademicCalendar`](src/main/java/com/inwoo/classtrack/calendar/AcademicCalendar.java)
가 매번 구한다. 종료일·진행 상태·캘린더가 모두 같은 규칙을 쓴다.

**AOP 로깅** — 서비스 계층 호출을 [`LoggingAspect`](src/main/java/com/inwoo/classtrack/aspect/LoggingAspect.java)
가 가로채 실행 시간과 성공/실패를 남긴다. 서비스 코드에는 로깅 코드가 없다.
컨트롤러는 `@LogExecution` 을 붙인 곳만 호출 인자를 기록한다.

**비동기 링크 검증** — 결과물 링크가 실제로 열리는지 외부 HTTP 로 확인한다.
저장 응답을 늦추지 않도록 커밋 후 별도 스레드에서 실행하며(`@Async` +
`@TransactionalEventListener(AFTER_COMMIT)`), MDC 를 `TaskDecorator` 로 전파해
비동기 로그에서도 요청 맥락이 이어진다.

**구현 현황 자동 수집** — `/dev` 화면의 표는 손으로 관리하지 않는다.
Spring 의 핸들러 매핑과 JPA Metamodel 을 읽어 만들기 때문에 코드를 고치면 그대로 반영된다.

## 배포

Vercel(프론트) + Railway(백엔드) + Neon(DB) 구성.
순서와 환경변수는 [DEPLOY.md](DEPLOY.md) 참고.

## 실행

### 1. 크리덴셜 설정

```bash
cp secrets.properties.example secrets.properties
```

`secrets.properties` 에 DB 접속 정보를 채운다. 이 파일은 `.gitignore` 대상이며,
없으면 같은 이름의 환경변수로 대체된다.

### 2. 백엔드 (포트 8085)

```bash
./mvnw spring-boot:run
```

프로필을 지정하지 않으면 `local` 로 뜬다. 운영용 설정은 `application-prod.yaml`.

### 3. 프론트엔드 (포트 5173)

```bash
cd frontend
npm install
npm run dev
```

Vite 프록시가 `/api` 요청을 8085 로 넘기므로 CORS 설정이 필요 없다.

## 설정 파일

| 파일 | 커밋 | 내용 |
|---|---|---|
| `application.yaml` | O | 공통 설정, 과목 목록, 공휴일 |
| `application-local.yaml` | O | 개발용 — SQL 로그, DEBUG 레벨 |
| `application-prod.yaml` | O | 운영용 — 스키마 검증, 오류 상세 숨김 |
| `secrets.properties` | **X** | DB 크리덴셜, 개발자 정보 |

공휴일은 `app.calendar.holidays` 에 적는다. 음력 공휴일(설날·추석 등)과 대체공휴일은
직접 채워야 한다.
