# Stage 1: Build the JAR using Maven
FROM maven:3.9-eclipse-temurin-17 AS builder

WORKDIR /build

# Copy pom.xml and download dependencies first (for Docker cache)
COPY pom.xml .
COPY fake-repo ./fake-repo
RUN mvn dependency:go-offline

# Copy the full source tree and build the application
COPY src ./src
RUN mvn clean package -DskipTests

# Debug: List the content of the target directory to verify JAR file was created
RUN ls -la /build/target/

# Stage 2: Minimal runtime image
FROM eclipse-temurin:17-jdk-jammy

WORKDIR /app

# Optional: add tini to manage signals properly
RUN apt-get update && apt-get install -y tini && rm -rf /var/lib/apt/lists/*

# Create directories and a non-root user
RUN useradd -m dyndrest
USER dyndrest

# Copy the built jar from the builder stage
COPY --from=builder /build/target/dyndrest*.jar /app/dyndrest.jar

# Debug: Verify the JAR file exists
RUN ls -la /app/

EXPOSE 8081

ENTRYPOINT ["/sbin/tini", "--"]
CMD ["java", "-jar", "dyndrest.jar"]
