FROM openjdk:17-jdk-alpine
WORKDIR /movie-ticket-booking
RUN apk --no-cache add maven
RUN mvn clean
RUN mvn package
COPY target/*.jar /movie-ticket-booking/user-service.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/movie-ticket-booking/user-service.jar"]