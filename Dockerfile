FROM amazoncorretto:21-alpine-jdk

RUN mkdir -p /app

ADD target/intelligence-feed-service-1.0.0-standalone.jar /app/app.jar
ADD resources /app/resources
ADD VERSION /app/VERSION
ADD run-service-in-docker /app/run-service

WORKDIR /app

CMD ["/app/run-service"]
