FROM openjdk:17-jdk-alpine
WORKDIR /movie-ticket-booking
COPY . ${WORKDIR}
RUN ./mvnw clean package
EXPOSE 8081
ENTRYPOINT ["java","-jar","target/*.jar"]