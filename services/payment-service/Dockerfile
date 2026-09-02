FROM eclipse-temurin:21-jre
WORKDIR /app
COPY target/*.jar app.jar
USER 1000
EXPOSE 8090
ENTRYPOINT ["java","-jar","/app/app.jar"]
