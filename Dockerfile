FROM ubuntu:20.04


# Note: Ensure the Dockerfile is in the same dir or above of all files meant to be copied into
# the image because Docker doesn't allow accessing any files outside of the same/child dir of
# where the Dockerfile is due to how the context of the build logic is needed before running
# any Docker commands.
#
# See:
#   - https://stackoverflow.com/questions/24537340/docker-adding-a-file-from-a-parent-directory


# Change default shell to Bash for better feature support/easier usage.
# Note: Removes the need for specifying an `ENTRYPOINT` since Bash will now
# be the default fallback.
#
# Login shells will correctly source `$HOME/.profile` but the if-statements
# for sourcing .bashrc don't work as expected within Docker. Thus, any modifications
# to the environment should be done within .profile (e.g. installing nvm).
# See:
#   - https://stackoverflow.com/questions/55206227/why-bashrc-is-not-executed-when-run-docker-container/74017557#74017557
SHELL [ "/bin/bash", "-lc" ]
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

# Set placeholder timezone so required package `tzdata` doesn't require interactive input
# See: https://stackoverflow.com/questions/44331836/apt-get-install-tzdata-noninteractive/44333806#44333806
RUN ln -fs /usr/share/zoneinfo/America/New_York /etc/localtime

# Since the Java app now bundles OpenJFX, no need to install it here
RUN apt-get clean && \
    apt-get update && \
    apt-get install -y --no-install-recommends \
        curl \
        git \
        sqlite3 \
        jq \
        openjdk-17-jdk \
        openjfx

# HOME isn't defined during image-building
ENV HOME=/root
# Change CWD from /root to /home (but don't modify `HOME` since user is root)
WORKDIR /home

# Copy the entire app (server/client) from the local filesystem to the Docker image
COPY . .

# Install nvm. Either set vars in .profile and use `bash -lc` as SHELL in Docker,
# or set the vars below in Dockerfile.
# Note: HOME isn't defined during image-building, so we defined it above
# ENV NVM_DIR="$HOME/.nvm"
# ENV NVM_SYMLINK_CURRENT=true
# ENV NVM_CURRENT_HOME="$NVM_DIR/current"
# ENV PATH="$NVM_CURRENT_HOME/bin:$PATH"
RUN ./nvm-install.sh

ENV ROOT_DIR=./
ENV CLIENT_DIR="${ROOT_DIR}/client"
ENV SERVER_DIR="${ROOT_DIR}/server"
ENV BUILD_DIR="${SERVER_DIR}/build/libs"
ENV WAR_FILE_NAME="anime-atsume.war"
ENV WAR_FILE_BUILD_PATH="${BUILD_DIR}/${WAR_FILE_NAME}"
ENV WAR_FILE_FINAL_PATH="./${WAR_FILE_NAME}"
ENV DB_FILE_NAME="anime_atsume.db"
ENV DB_FILE_BUILD_PATH="${BUILD_DIR}/${DB_FILE_NAME}"
ENV DB_FILE_FINAL_PATH="./${DB_FILE_NAME}"

ENV HTTP_PORT=8080
ENV HTTPS_PORT=8443


# Build the app if not already done before attempting Docker image generation
# Copy build-output files to root dir for ease of use
RUN if ! [[ -f "${WAR_FILE_FINAL_PATH}" ]]; then \
        if ! [[ -f "${WAR_FILE_BUILD_PATH}" ]]; then \
            ./index.sh build -Crs; \
        else \
            cp "${WAR_FILE_BUILD_PATH}" "${WAR_FILE_FINAL_PATH}"; \
            cp "${DB_FILE_BUILD_PATH}" "${DB_FILE_FINAL_PATH}"; \
        fi; \
    fi;

EXPOSE ${HTTP_PORT}
EXPOSE ${HTTPS_PORT}

# Limit memory usage in website host via `JAVA_OPTS=-Xmx640m` (replace number with MB of RAM desired).
# Modify Spring .properties with `-Dmy.prop.name=myVal` if running directly from `java`
# or `-e JAVA_OPTS=-Dmy.prop.name=myVal` if running from `docker run`.
CMD java ${JAVA_OPTS} -Dglass.platform=Monocle -Dmonocle.platform=Headless -jar "${WAR_FILE_FINAL_PATH}" --server.http.port=${HTTP_PORT} --server.https.port=${HTTPS_PORT}
