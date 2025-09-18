# Stage 1: Build the JAR using Maven
FROM maven:3.9-amazoncorretto-17 AS builder

WORKDIR /build

# Copy pom.xml and download dependencies first (for Docker cache)
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the full source tree and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Minimal runtime image
FROM amazoncorretto:17-alpine-jdk

WORKDIR /app

# Optional: add tini to manage signals properly
RUN apk add --no-cache tini

# Create a non-root user
RUN adduser -D dyndrest
USER dyndrest

# Copy the built jar from the builder stage
COPY --from=builder /build/target/dyndrest*.jar /app/dyndrest.jar

# Optionally copy default config
# COPY dyndrest.yml .
# COPY logback.xml .

ENTRYPOINT ["/sbin/tini", "--"]
CMD ["java", "-jar", "dyndrest.jar"]
