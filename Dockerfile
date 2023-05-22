FROM openjdk:8
EXPOSE 8080
ADD target/vietstack.jar vietstack.jar
ENTRYPOINT ["java","-jar","vietstack.jar"]