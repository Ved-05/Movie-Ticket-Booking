FROM openjdk:17-jdk-alpine
WORKDIR /movie-ticket-booking
COPY target/*.jar /movie-ticket-booking/booking-service.jar
COPY data /movie-ticket-booking/data
EXPOSE 8080
ENTRYPOINT ["java","-jar","/movie-ticket-booking/booking-service.jar"]