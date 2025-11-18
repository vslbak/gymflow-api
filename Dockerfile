FROM eclipse-temurin:21-jdk
#VOLUME /tmp
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8380
ENTRYPOINT ["java", "-jar", "app.jar"]
