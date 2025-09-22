# Multi-stage build: build the Spring Boot fat JAR, then run it on a slim JRE image

# ---- Build stage ----
FROM maven:3.9.8-eclipse-temurin-17 AS build
WORKDIR /src

# Copy pom and sources
COPY pom.xml .
COPY src ./src
# Project relies on a local fake repository used during build
COPY fake-repo ./fake-repo

# Build the application (skip tests for faster container build)
RUN mvn -B -DskipTests package

# ---- Runtime stage ----
FROM eclipse-temurin:17-jre-alpine

# Working directory for the application data and configs
# Mount your host directory to /app to provide dyndrest.yml, zone.yml, and to persist the H2 DB files
WORKDIR /app

# Expose the default HTTP port used by DynDRest when running in container
EXPOSE 8080

# Copy the built Spring Boot fat JAR to a fixed path outside /app so mounting /app won't hide the JAR
# Keep WORKDIR at /app with no subdirectories for config and H2 database files
COPY --from=build /src/target/dyndrest-*.jar /dyndrest.jar

# By default Spring Boot loads external config from the working directory (/app)
# The H2 URL in application.yml is jdbc:h2:file:./dyndrest so DB files will be created/used in /app
ENTRYPOINT ["java","-jar","/dyndrest.jar"]
