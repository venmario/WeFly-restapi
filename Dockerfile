# Use a base image with maven and Java temurin
FROM maven:3.8.6-eclipse-temurin-17-alpine

WORKDIR /app

COPY . ./

RUN mvn install -DskipTests

# Set the entry point to run your application
ENTRYPOINT ["java","-jar","target/WeFly_App_Final_Project.jar"]