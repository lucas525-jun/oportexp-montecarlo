# Use the official Maven image to build the application
FROM maven:3.8.5-openjdk-17-slim AS build

# Set the working directory
WORKDIR /build

ADD . .
#COPY pom.xml .
#COPY src src

RUN mvn package -DskipTests

# Use a smaller base image to run the application
FROM openjdk:17-jdk AS runtime

RUN mkdir /app
WORKDIR /app

# Copy the jar file from the previous stage
COPY --from=build /build/target/seemop-montecarlo.jar seemop-montecarlo.jar

# Expose the application port (default is 8080)
EXPOSE 8085

# Run the application
ENTRYPOINT ["java", "-jar", "seemop-montecarlo.jar"]