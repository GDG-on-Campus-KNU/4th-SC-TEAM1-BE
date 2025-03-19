# 1. JDK 21을 포함한 이미지를 사용하여 Java 애플리케이션을 실행할 수 있는 환경을 준비합니다.
FROM openjdk:21-jdk-slim

# 2. 작업 디렉토리를 설정합니다.
WORKDIR /app

# 3. 기본 시간대를 서울(Asia/Seoul)로 설정합니다.
ENV TZ=Asia/Seoul
RUN ln -sf /usr/share/zoneinfo/Asia/Seoul /etc/localtime

# 4. Gradle 빌드 후 생성된 JAR 파일을 컨테이너 내의 /app 디렉토리로 복사합니다.
COPY build/libs/Todak-0.0.1-SNAPSHOT.jar /app/todak.jar

# 5. 환경 변수를 설정하기 위한 ARG 선언
ARG APP_CONFIG

# 6. 디렉터리 생성 후 환경 변수로 받은 내용을 파일로 생성합니다.
RUN mkdir -p /app/src/main/resources && printf "%s" "$APP_CONFIG" > /app/src/main/resources/application-dev.properties

# 7. 애플리케이션을 실행하는 명령어를 설정합니다.
CMD ["java", "-Duser.timezone=Asia/Seoul", "-jar", "todak.jar"]

# 8. 애플리케이션이 사용하는 포트를 설정합니다.
EXPOSE 8080
