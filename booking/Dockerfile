FROM openjdk:17-jdk-alpine
WORKDIR /movie-ticket-booking
COPY . ${WORKDIR}
RUN ./mvnw clean package
RUN cp target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java","-jar","app.jar"]