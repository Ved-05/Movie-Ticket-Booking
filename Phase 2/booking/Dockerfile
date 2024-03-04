FROM eclipse-temurin:17

COPY pom.xml  /pom.xml
COPY src      /src
COPY target   /target
COPY data   /data

RUN apt-get update

RUN apt-get -y install maven

RUN mvn -Dmaven.test.skip=true package

RUN mv /target/*.jar /target/app.jar

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "/target/app.jar"]