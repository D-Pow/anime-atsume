FROM ubuntu:18.04


# Note: Ensure the Dockerfile is in the same dir or above of all files meant to be copied into
# the image because Docker doesn't allow accessing any files outside of the same/child dir of
# where the Dockerfile is due to how the context of the build logic is needed before running
# any Docker commands.
#
# See:
#   - https://stackoverflow.com/questions/24537340/docker-adding-a-file-from-a-parent-directory


# Change default shell to Bash for better feature support/easier usage
SHELL [ "/bin/bash", "-c" ]
# Change CWD from <root> to $HOME
# WORKDIR "/home"

# `docker` flags useful for debugging:
#   `--progress=plain`: show STDOUT/STDERR.
#   `--no-cache`: force rebuilding all dependencies, packages, etc. that would usually be cached.
#
# Passing flags to `ARG` entries for customizing `Dockerfile` run:
#   `--build-arg myArg=myValue`
#       From: https://stackoverflow.com/questions/34254200/how-to-pass-arguments-to-a-dockerfile/34254700#34254700

RUN apt-get clean && \
    apt-get update && \
    apt-get install -y \
        curl \
        git \
        sqlite3 \
        openjdk-8-jdk \
        openjfx=8u161-b12-1ubuntu2 \
        libopenjfx-jni=8u161-b12-1ubuntu2 \
        libopenjfx-java=8u161-b12-1ubuntu2 \
        jq \
        && \
    apt-mark hold openjfx libopenjfx-jni libopenjfx-java

ENV SHELL=/bin/bash

WORKDIR /home

ARG CLIENT_DIR=./client
ARG SERVER_DIR=./server
ARG BUILD_DIR=${SERVER_DIR}/build/libs
ARG WAR_FILE=${BUILD_DIR}/*.war
ARG DB_FILE=${BUILD_DIR}/*.db

RUN ./index.sh deploy

# Copy the entire app (server/client) from the local filesystem to the Docker image
COPY . .

# In particular (and probably not necessary) copy the build's main artifacts (`.war`/`.db`) to
# the Docker image
COPY ${WAR_FILE} ./anime-atsume.war
COPY ${DB_FILE} ./anime_atsume.db

EXPOSE 8080

CMD java ${JAVA_OPTS} -Dglass.platform=Monocle -Dmonocle.platform=Headless -jar anime-atsume.war --server.port=${PORT:-8080}
