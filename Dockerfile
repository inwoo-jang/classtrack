# ============================================================
# 프론트와 백엔드를 하나의 JAR 로 묶어 실행한다.
#
# Vue 빌드 결과물을 Spring 의 static/ 에 넣으므로 같은 주소에서 서빙되고,
# 따라서 CORS 설정이 필요 없다 — 개발 중 Vite 프록시를 쓰던 것과 같은 구조다.
#
# 단계를 나눈 이유: 마지막 이미지에 Node 와 Maven 을 남기지 않기 위해서다.
# 빌드 도구는 중간 단계에서만 쓰이고 최종 이미지에는 JRE 와 JAR 만 들어간다.
# ============================================================

# ---------- 1) 프론트엔드 빌드 ----------
FROM node:24-alpine AS frontend

WORKDIR /app/frontend

# 의존성 파일만 먼저 복사한다. 소스만 바뀌었을 때 npm ci 를 건너뛰기 위한 캐시 전략.
COPY frontend/package.json frontend/package-lock.json ./
RUN npm ci

COPY frontend/ ./
RUN npm run build


# ---------- 2) 백엔드 빌드 ----------
FROM maven:3.9-eclipse-temurin-21 AS backend

WORKDIR /app

COPY pom.xml ./
RUN mvn -B -q dependency:go-offline

COPY src ./src
# 프론트 결과물을 정적 리소스로 심는다
COPY --from=frontend /app/frontend/dist ./src/main/resources/static

RUN mvn -B -q clean package -DskipTests


# ---------- 3) 실행 ----------
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# root 로 돌리지 않는다
RUN addgroup -S app && adduser -S app -G app
USER app

COPY --from=backend --chown=app:app /app/target/*.jar app.jar

# PaaS 는 PORT 환경변수로 포트를 지정한다. application.yaml 의 server.port 가 이 값을 읽는다.
EXPOSE 8080

# 무료 플랜은 메모리가 512MB 로 작다. 실측 기준으로 잡은 값이다.
#
#   힙 50%(=256MB) + 메타스페이스 128MB + 코드캐시·스레드 ≈ 450MB
#
# MaxRAMPercentage 는 힙만 제한한다는 점이 함정이다. 이 앱은 Spring·Hibernate·AspectJ 를
# 모두 쓰다 보니 메타스페이스만 90MB 가 넘어서, 힙을 70% 로 두면 합계가 512MB 를 넘긴다.
# 컨테이너가 한도를 넘으면 JVM 예외가 아니라 OS 가 프로세스를 죽인다(OOMKilled).
#
# UseSerialGC: 코어가 적은 작은 컨테이너에서는 병렬 GC 의 스레드 오버헤드가 손해다.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=50", "-XX:MaxMetaspaceSize=128m", "-XX:+UseSerialGC", "-XX:+ExitOnOutOfMemoryError", "-jar", "/app/app.jar"]
