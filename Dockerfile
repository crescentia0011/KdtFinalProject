FROM eclipse-temurin:21-jre

WORKDIR /app

ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar

ENV TZ=Asia/Seoul

EXPOSE 80

ENTRYPOINT ["java", "-Doracle.jdbc.timezoneAsRegion=false", "-Duser.timezone=Asia/Seoul", "-jar", "app.jar"]
