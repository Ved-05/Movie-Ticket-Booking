FROM openjdk:17-jdk-alpine
WORKDIR /movie-ticket-booking
RUN apk --no-cache add maven
RUN mvn package
COPY target/*.jar /movie-ticket-booking/wallet-service.jar
EXPOSE 8082
ENTRYPOINT ["java","-jar","/movie-ticket-booking/wallet-service.jar"]