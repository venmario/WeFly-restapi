# Use a base image with Java
FROM eclipse-temurin:17-jdk-alpine
# Copy the built jar file into the image

RUN #mkdir /app
RUN mkdir database_seeder
RUN mkdir properties

COPY database_seeder/* /database_seeder
COPY properties/* /properties
COPY target/*.jar /app.jar

#WORKDIR /app

# Set the entry point to run your application
ENTRYPOINT ["java","-jar","/app.jar"]