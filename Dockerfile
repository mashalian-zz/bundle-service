FROM openjdk:17-alpine
EXPOSE 8080
ADD target/bundle-service-docker.jar bundle-service-docker.jar
ENTRYPOINT ["java","-jar","/bundle-service-docker.jar"]