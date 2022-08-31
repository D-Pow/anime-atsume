FROM ubuntu:18.04

RUN apt-get clean && \
    apt-get update && \
    apt-get install -y sqlite3 openjdk-8-jre openjfx=8u161-b12-1ubuntu2 libopenjfx-jni=8u161-b12-1ubuntu2 libopenjfx-java=8u161-b12-1ubuntu2 && \
    apt-mark hold openjfx libopenjfx-jni libopenjfx-java

ARG WAR_FILE=./build/libs/*.war
ARG DB_FILE=./build/libs/*.db

ARG BUILD_RUN_CMD="java ${JAVA_OPTS} -Dglass.platform=Monocle -Dmonocle.platform=Headless -jar anime-atsume.war --server.port=${PORT:-8080}"

RUN gradle clean build

COPY ${WAR_FILE} anime-atsume.war
COPY ${DB_FILE} anime_atsume.db

RUN chmod a+rwx anime_atsume.db

EXPOSE 8080

RUN "$BUILD_RUN_CMD"
