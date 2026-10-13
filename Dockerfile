# 1단계: 빌드 (JDK로 jar 파일 만들기)
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app
COPY . .
# Windows에서 올린 gradlew의 줄바꿈(CRLF)을 리눅스용(LF)으로 바꾸고 실행 권한 부여
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew && ./gradlew bootJar -x test --no-daemon

# 2단계: 실행 (가벼운 JRE에 jar만 복사)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
ENV TZ=Asia/Seoul
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]