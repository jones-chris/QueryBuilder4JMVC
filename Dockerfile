FROM openjdk:8

ARG environment
ENV environment=${environment}

RUN mkdir /qb4j
WORKDIR /qb4j
COPY /target/querybuilder4jmvc-1.jar .
RUN mkdir /qb4j/data
COPY /data /qb4j/data

EXPOSE 8080

CMD ["java", "-jar", "/qb4j/querybuilder4jmvc-1.jar"]
