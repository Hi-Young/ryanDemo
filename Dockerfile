FROM eclipse-temurin:8-jdk-alpine
VOLUME /tmp
COPY target/bruce-demo-0.0.1.jar app.jar
ENTRYPOINT ["java", "-Duser.timezone=Asia/Shanghai", "-jar", "/app.jar"]
