FROM openjdk:17-jdk-alpine
WORKDIR /movie-ticket-booking
COPY pom.xml .
COPY src src
COPY data /movie-ticket-booking/data
RUN apk --no-cache add maven
RUN mvn clean package
COPY target/*.jar booking-service.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","booking-service.jar"]