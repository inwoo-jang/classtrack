# 배포 가이드

프론트는 Vercel, 백엔드는 Render, DB 는 Neon 에 둔다.

```
Vercel (Vue)  ──HTTPS──→  Render (Spring Boot)  ──→  Neon (PostgreSQL)
```

> Railway 설정(`railway.toml`)도 남겨두었다. Dockerfile 기반이라 둘 중
> 어느 쪽에 올려도 코드는 그대로다.

세 곳이 서로의 주소를 알아야 하므로 **순서가 있다.** 백엔드를 먼저 올려 주소를 얻고,
그 주소로 프론트를 빌드하고, 다시 백엔드에 프론트 주소를 CORS 로 등록한다.

---

## 0. 준비 — DB 비밀번호

Neon 콘솔에서 비밀번호를 새로 발급받는다. 로컬 `secrets.properties` 의 `DB_PASSWORD` 도
같은 값으로 바꾼다.

## 1. Render — 백엔드

1. [render.com](https://render.com) 가입 → GitHub 연동
2. **New → Blueprint** → 저장소 `classtrack` 선택
   - 저장소의 `render.yaml` 을 읽어 서비스를 자동으로 만든다
   - Blueprint 대신 **New → Web Service** 로 만들어도 된다.
     그때는 Runtime 을 **Docker** 로, Health Check Path 를 `/actuator/health` 로 지정한다
3. 배포 전에 값을 물어보는 환경변수를 채운다:

   | 이름 | 값 |
   |---|---|
   | `DB_URL` | `secrets.properties` 의 `DB_URL` |
   | `DB_USERNAME` | `secrets.properties` 의 `DB_USERNAME` |
   | `DB_PASSWORD` | 0번에서 새로 발급한 값 |
   | `CORS_ALLOWED_ORIGINS` | 아직 모르므로 **비워두거나 아무 값** — 2번 이후 3번에서 채운다 |

   `SPRING_PROFILES_ACTIVE=prod` 는 `render.yaml` 에 이미 들어 있다.

   > `PORT` 는 Render 가 자동으로 넣는다(기본 10000). 직접 등록하지 말 것.

4. 배포가 끝나면 `https://classtrack-api.onrender.com` 같은 주소를 받는다
5. 확인: `<주소>/actuator/health` 가 `{"status":"UP"}` 을 돌려주면 성공

### 무료 플랜에서 알아둘 것

- **15분 동안 요청이 없으면 잠든다.** 다음 첫 요청은 컨테이너를 다시 띄우므로
  **50초~1분** 걸린다. 화면이 한참 안 뜨는 것처럼 보이지만 고장이 아니다.
- 메모리가 512MB 라 Dockerfile 에서 힙을 70% 로 제한하고 SerialGC 를 쓴다.
- 첫 빌드는 5~10분 걸릴 수 있다.

## 2. Vercel — 프론트

1. [vercel.com](https://vercel.com) 가입 → GitHub 연동
2. **Add New → Project → `classtrack`** 선택
3. 설정에서 **Root Directory 를 `frontend` 로 지정** (중요 — 기본값이면 빌드가 실패한다)
4. **Environment Variables** 에 등록:

   | 이름 | 값 |
   |---|---|
   | `VITE_API_BASE_URL` | 1-4 에서 받은 Render 주소 (끝에 `/` 없이) |

5. Deploy → `https://classtrack.vercel.app` 같은 주소를 받는다

`frontend/vercel.json` 이 하는 일 (JSON 이라 파일에는 주석을 달 수 없어 여기 적는다):

| 설정 | 이유 |
|---|---|
| `rewrites` → `/index.html` | Vue Router 가 history 모드라 `/courses/1` 로 새로고침하면 서버에 그 경로를 요청한다. 그런 파일은 없으므로 index.html 을 돌려주고 라우팅은 브라우저가 하게 한다. 실제 파일(assets, 아이콘)은 Vercel 이 먼저 찾아서 그대로 준다 |
| `headers` → `/assets/*` 1년 캐시 | 번들 파일명에 내용 해시가 붙으므로, 내용이 바뀌면 이름도 바뀐다. 같은 이름이면 같은 내용이 보장되어 영구 캐시가 안전하다 |

## 3. 다시 Render — CORS 등록

프론트와 API 의 출처가 다르므로, 백엔드가 프론트 주소를 허용해야 한다.
Render 서비스의 **Environment** 에서 값을 채운다:

| 이름 | 값 |
|---|---|
| `CORS_ALLOWED_ORIGINS` | `https://classtrack.vercel.app,https://classtrack-*.vercel.app` |

두 번째 항목은 Vercel 이 커밋마다 만드는 미리보기 도메인용 와일드카드다.
저장하면 Render 가 자동으로 재배포한다.

---

## 확인

| 확인할 것 | 기대 |
|---|---|
| `<render>/actuator/health` | `{"status":"UP"}` |
| `<render>/api/courses` | JSON 배열 |
| `<render>/api/dev/overview` | **404** (prod 에서는 막힘) |
| `<vercel>` 접속 | 대시보드가 뜨고 데이터가 보임 |
| `<vercel>/courses/1` 로 새로고침 | 404 가 아니라 화면이 뜸 |
| 강의 등록·수정 | 저장됨 (CORS 통과) |

### 자주 나는 문제

**화면은 뜨는데 데이터가 안 나온다**
브라우저 콘솔에 `blocked by CORS policy` 가 있는지 본다. 3번을 안 했거나
`CORS_ALLOWED_ORIGINS` 의 주소에 오타·끝 슬래시가 있는 경우다.

**조회는 되는데 저장이 안 된다**
`PATCH`·`PUT`·`DELETE` 는 브라우저가 `OPTIONS` 로 먼저 물어본다(preflight).
Network 탭에서 `OPTIONS` 요청이 403 이면 CORS 설정 문제다.

**Healthcheck / 배포가 실패한다** (Render·Railway 공통)

헬스체크 실패는 대부분 <b>앱이 아예 뜨지 않은 것</b>이다. 포트가 열리지 않으니 응답이 없다.
Deploy Logs 에서 `APPLICATION FAILED TO START` 블록을 찾으면 원인이 한 줄로 적혀 있다.

| 로그의 Description | 원인 |
|---|---|
| `Could not resolve placeholder 'DB_URL'` | 변수 이름 오타 또는 미등록 |
| `password authentication failed` | `DB_PASSWORD` 가 틀림 (Neon 에서 리셋했다면 새 값인지 확인) |
| `Unable to determine Dialect` | `DB_URL` 이 잘못됐거나 DB 에 접속 불가 |
| `Schema-validation: missing table/column` | `ddl-auto: validate` 인데 스키마가 엔티티와 다름 |
| `UnknownHostException` | `DB_URL` 호스트 오타 |

로그에 `Started ClasstrackApplication` 이 찍혔는데도 실패한다면 그때는 포트 문제다.
`Tomcat started on port ...` 줄의 포트가 플랫폼이 준 `PORT` 와 같은지 본다.

**Vercel 빌드가 실패한다**
Root Directory 가 `frontend` 인지 확인한다.

**첫 접속이 1분 가까이 걸린다**
Render 무료 플랜이 잠들어 있다가 깨어나는 중이다. 고장이 아니다.
계속 깨어 있게 하려면 유료 플랜으로 올리거나 주기적으로 호출해야 한다.

---

## 로컬에서 배포본과 같은 조건으로 실행하기

```bash
cd frontend && npm run build && cd ..
rm -rf src/main/resources/static && mkdir -p src/main/resources/static
cp -R frontend/dist/. src/main/resources/static/
./mvnw -q clean package -DskipTests

SPRING_PROFILES_ACTIVE=prod PORT=9099 \
  DB_URL=... DB_USERNAME=... DB_PASSWORD=... \
  java -jar target/classtrack-0.0.1-SNAPSHOT.jar
```

이 경우 프론트가 같은 서버에서 서빙되므로 CORS 없이 동작한다.
