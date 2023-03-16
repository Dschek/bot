FROM openjdk:19-jdk-alpine

WORKDIR /home/chatgpt
COPY ./build/libs/chatgpt-0.0.1-SNAPSHOT.jar chatgpt.jar

EXPOSE 8080

CMD java -jar chatgpt.jar