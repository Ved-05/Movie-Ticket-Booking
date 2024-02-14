# Use an official OpenJDK runtime as a parent image
FROM openjdk:17

# Set the working directory to /app
WORKDIR /showService

# Copy the current directory contents into the container at /showService
COPY . .

# Install Maven
RUN microdnf install -y maven

# Build the projects with Maven
RUN mvn clean install

# Make port 8080 available to the world outside this container


ENTRYPOINT ["java" ,"-jar","target/user-0.0.1.jar"]