FROM openjdk:17-jdk-alpine
WORKDIR /movie-ticket-booking
RUN apk --no-cache add maven
RUN mvn package
COPY target/*.jar /movie-ticket-booking/booking-service.jar
COPY data /movie-ticket-booking/data
EXPOSE 8081
ENTRYPOINT ["java","-jar","/movie-ticket-booking/booking-service.jar"]