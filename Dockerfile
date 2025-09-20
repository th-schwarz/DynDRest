# Stage 1: Build the JAR using Maven
FROM maven:3.9-amazoncorretto-17 AS builder

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
FROM amazoncorretto:17-alpine-jdk

WORKDIR /app

# Optional: add tini to manage signals properly
RUN apk add --no-cache tini
RUN apk add --no-cache useradd

# Create directories
RUN mkdir -p /app/config /app/log

# Create a non-root user and change ownership
RUN useradd -m dyndrest

# Switch to non-root user
USER dyndrest

# Copy the built jar from the builder stage
COPY --from=builder /build/target/dyndrest*.jar /app/dyndrest.jar
#chown -Rv dyndrest:dyndrest /app

# Debug: Verify the JAR file exists
RUN ls -la /app/
RUN ls -la /sbin

EXPOSE 8081

ENTRYPOINT ["/sbin/tini", "--"]
CMD ["java", "-jar", "dyndrest.jar"]
