# Use a base image with Java
FROM maven:3.8.6-eclipse-temurin-17-alpine
# Copy the built jar file into the image

WORKDIR /app

COPY . ./

RUN mvn package -DskipTests

# Set the entry point to run your application
ENTRYPOINT ["java","-jar","target/WeFly_App_Final_Project.jar"]