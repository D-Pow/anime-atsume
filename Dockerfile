FROM ubuntu:20.04


# Note: Ensure the Dockerfile is in the same dir or above of all files meant to be copied into
# the image because Docker doesn't allow accessing any files outside of the same/child dir of
# where the Dockerfile is due to how the context of the build logic is needed before running
# any Docker commands.
#
# See:
#   - https://stackoverflow.com/questions/24537340/docker-adding-a-file-from-a-parent-directory


# Change default shell to Bash for better feature support/easier usage
SHELL [ "/bin/bash", "-c" ]

ENV SHELL=/bin/bash

# `docker` flags useful for debugging:
#   `--progress=plain`: show STDOUT/STDERR.
#   `--no-cache`: force rebuilding all dependencies, packages, etc. that would usually be cached.
#
# Passing flags to `ARG` entries for customizing `Dockerfile` run:
#   `--build-arg myArg=myValue`
#       From: https://stackoverflow.com/questions/34254200/how-to-pass-arguments-to-a-dockerfile/34254700#34254700

# Activate CLI logging during `docker build` via `--build-arg BUILD_VERBOSE=true`
#
# See:
#   - https://stackoverflow.com/questions/64804749/why-is-docker-build-not-showing-any-output-from-commands/67682576#67682576
#   - https://vsupalov.com/docker-build-time-env-values/#here-s-how
#   - https://vsupalov.com/docker-arg-vs-env
ARG BUILD_VERBOSE=true
ENV BUILDKIT_PROGRESS=${BUILD_VERBOSE:+plain}

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

# Change CWD from <root> to $HOME
WORKDIR /home

ENV ROOT_DIR=./
RUN export ROOT_DIR="$(realpath -se .)"
ENV CLIENT_DIR="${ROOT_DIR}/client"
ENV SERVER_DIR="${ROOT_DIR}/server"
ENV BUILD_DIR="${SERVER_DIR}/build/libs"
ENV WAR_FILE="${BUILD_DIR}/anime-atsume.war"
ENV DB_FILE="${BUILD_DIR}/anime_atsume.db"

# Copy the entire app (server/client) from the local filesystem to the Docker image
COPY . .

# Build the app if not already done before attempting Docker image generation
# Copy build-output files to root dir for ease of use
RUN if ! [[ -d "${BUILD_DIR}" ]]; then ./index.sh build -r; fi

EXPOSE 8080

CMD java ${JAVA_OPTS} -Dglass.platform=Monocle -Dmonocle.platform=Headless -jar "${WAR_FILE}" --server.port=${PORT:-8080}
