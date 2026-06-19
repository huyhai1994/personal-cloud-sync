FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/personal_cloud_sync-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 20000

CMD ["java", "-jar", "app.jar"]