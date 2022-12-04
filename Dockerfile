#
# Build stage
#
FROM maven:3.6.0-jdk-11-slim AS build
# copy sourcecode
COPY src /opt/dyndrest/src
COPY pom.xml /opt/dyndrest/
# copy fake-repo dependency
COPY fake-repo /opt/dyndrest/fake-repo
# do the build
RUN mvn -f /opt/dyndrest/pom.xml clean package

#
# Package stage
#
FROM openjdk:11-jre-slim
# get the compiled JAR
COPY --from=build /opt/dyndrest/target/dyndrest-0.2-SNAPSHOT.jar /opt/dyndrest/dyndrest.jar

WORKDIR /opt/dyndrest
EXPOSE 8081
ENTRYPOINT ["java","-jar","/opt/dyndrest/dyndrest.jar"]
