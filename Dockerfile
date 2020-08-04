FROM openjdk:8-jre-alpine
EXPOSE 8080
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} app.jar
RUN addgroup -S app && adduser -S app -G app
USER app
ENTRYPOINT ["java","-jar","/app.jar"]
