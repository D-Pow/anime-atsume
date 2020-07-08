FROM ubuntu:18.04

RUN apt-get clean && \
    apt-get update && \
    apt-get install -y openjdk-8-jre openjfx=8u161-b12-1ubuntu2 libopenjfx-jni=8u161-b12-1ubuntu2 libopenjfx-java=8u161-b12-1ubuntu2 && \
    apt-mark hold openjfx libopenjfx-jni libopenjfx-java

ARG WAR_FILE=./build/libs/*.war

COPY ${WAR_FILE} cookie-getter.war

EXPOSE 8080

CMD ["java", "-Dglass.platform=Monocle", "-Dmonocle.platform=Headless", "-jar", "cookie-getter.war"]
