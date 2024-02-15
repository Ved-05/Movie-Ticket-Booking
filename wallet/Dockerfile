FROM openjdk:17-jdk-alpine
WORKDIR /movie-ticket-booking
COPY pom.xml .
COPY src src
RUN apk --no-cache add maven
RUN mvn clean package
COPY target/*.jar wallet-service.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","wallet-service.jar"]