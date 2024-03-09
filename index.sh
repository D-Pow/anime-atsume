#!/usr/bin/env bash

declare rootDir="$(realpath -se "$(dirname "${BASH_SOURCE[@]}")")"
declare clientDir="${rootDir}/client"
declare serverDir="${rootDir}/server"
declare buildDir="${serverDir}/build/libs"
declare feBuildDir="$(realpath "${rootDir}/$(
    cat "${clientDir}/package.json" \
    | grep -E '\bbuildOutputDir' \
    | awk '{ print $2 }' \
    | sed -E 's/"//g; s|\.*/?|\./|'
)")"

# Obtained from https://freedns.afraid.org
# If one doesn't work, try the other (during dev, I requested the cert too many times)
declare domainNames=(
    anime-atsume.letz.dev
    anime-atsume.cloudwatch.net
)


_egrep() {
    declare _egrepCommandFlag='-P';
    declare perlRegexSupported="$(echo 'true' | grep -P 'u' 2>/dev/null)";

    if [[ -z "$perlRegexSupported" ]]; then
        _egrepCommandFlag='-E';
    fi;

    grep --exclude-dir={node_modules,.git,.idea,lcov-report} --color=auto $_egrepCommandFlag "$@"
}


repoOwnerAndName() {
    git remote -v | awk '{ print $2 }' | sed -E 's~.*/([^/]+/[^/]+).git$~\1~' | uniq
}

dockerRegistryUrl() {
    # Docker registries are lowercase in GitHub
    echo "ghcr.io/$(repoOwnerAndName | awk '{ print tolower($0) }')"
}

dockerImageExists() {
    docker manifest inspect "$1" &>/dev/null
}


clean() (
    cd "${clientDir}"
    npm run clean

    cd "${serverDir}"
    ./gradlew clean
)


config() (
    cd "${serverDir}"

    ./gradlew -q printSrcInfo
    ./gradlew -q printSrcFiles
    ./gradlew -q printDependencies
)


run() {
    declare _runFrontend=
    declare _runBackend=
    declare OPTIND=1

    while getopts ":fb" opt; do
        case "$opt" in
            f)
                _runFrontend=true
                ;;
            b)
                _runBackend=true
                ;;
            *)
                # Forward options to underlying command
                break
                ;;
        esac
    done

    shift $(( OPTIND - 1 ))

    if [[ -z "$_runFrontend" ]] && [[ -z "$_runBackend" ]]; then
        _runFrontend=true
        _runBackend=true
    fi

    declare _runChildPids=()
    declare _runChildExitCodes=()
    declare _runChildExitCodesSum=0

    if [[ -n "$_runFrontend" ]]; then
        (
            cd "${clientDir}"
            npm start
        ) &

        _runChildPids+=("$!")
    fi

    if [[ -n "$_runBackend" ]]; then
        (
            cd "${serverDir}"
            ./gradlew clean bootRun
        ) &

        _runChildPids+=("$!")
    fi

    declare _runChildPid=
    for _runChildPid in "${_runChildPids[@]}"; do
        wait "$_runChildPid"

        _runChildExitCodes+=("$?")

        (( _runChildExitCodesSum += ${_runChildExitCodes[${#_runChildExitCodes[@]}-1]} ))
    done

    # Doesn't work, likely due to `set -m` not being used at the top of this script.
    # However, setting it causes issues with the `wait` call, so leave it as-is for now.
    # Goal was to make Ctrl-C kill all child processes, but alas, while I've done this
    # before, it's failing now, so I'll just live with the double Ctrl-C for now.
    #
    # trap "kill ${_runChildPids[@]}" EXIT QUIT INT TERM

    return $_runChildExitCodesSum
}


build() (
    declare USAGE="${FUNCNAME[0]} [OPTIONS...]
    Builds the app.

    Options:
        -c  |   Clean build-output files before building.
        -f  |   Clean resources as well as build-output files.
        -r  |   Copy build output files to app root directory.
        -m  |   Builds app with headless Selenium instance.
        -v  |   Verbose build output.
        -h  |   Print this help message and exit.
    "
    declare _buildCleanFirst=
    declare _buildCopyFilesToRootDir=
    declare _buildHeadless=
    declare _buildVerbose=
    declare OPTIND=1

    while getopts ":fcrvmh" opt; do
        case "$opt" in
            c)
                clean
                ;;
            f)
                _buildCleanFirst=true
                ;;
            r)
                _buildCopyFilesToRootDir=true
                ;;
            m)
                # `-m` represents "Monocle" which is the headless JavaFX implementation
                _buildHeadless=true
                ;;
            v)
                _buildVerbose=true
                ;;
            h)
                echo "$USAGE" >&2
                (
                    cd "${serverDir}"
                    # Same as `./gradlew printCommands` except with colored output
                    ./gradlew -q tasks --all
                )

                return 1
                ;;

            *)
                # Forward options to underlying command
                break
                ;;
        esac
    done

    shift $(( OPTIND - 1 ))

    declare _buildGradleOpts="${_buildCleanFirst:+cleanAll} ${_buildHeadless:+-Dheadless=true} ${_buildVerbose:+--console plain}"

    (
        cd "${serverDir}"
        ./gradlew ${_buildGradleOpts} build
    )

    if [[ -n "$_buildCopyFilesToRootDir" ]]; then
        cp -R "${buildDir}/*" "${rootDir}"
    fi
)


checkBuildOutputDir() (
    cd "${serverDir}"

    if ! [[ -d "${buildDir}" ]]; then
        clean
        build "$@"
    fi
)


hashJar() (
    declare jarFile="${1:-${buildDir}/*.[jw]ar}"
    declare hashLength="${2:-8}"

    sha256sum $jarFile | _egrep -o --color=never "^.{${hashLength}}"
)


dockerBuild() (
    declare _dockerBuildFreshJar=
    declare _dockerBuildVerbose=
    declare OPTIND=1

    while getopts ":bv" opt; do
        case "$opt" in
            b)
                _dockerBuildFreshJar=true
                ;;
            v)
                _dockerBuildVerbose=true
                ;;
            *)
                # Forward options to `docker` command
                break
                ;;
        esac
    done

    shift $(( OPTIND - 1 ))

    build -rm ${_dockerBuildFreshJar:+-c}

    (
        command -v docker &>/dev/null && \
            docker build ${_dockerBuildVerbose:+--progress=plain} -t anime-atsume $@ .
    )
)

dockerClean() (
    declare _dockerId=

    for _dockerId in $(
        docker ps -a \
            | tail -n "+2" \
            | _egrep -i 'anime|none' \
            | awk '{ print $1 }'
    ); do
        docker rm "$_dockerId"
    done

    for _dockerId in $(
        docker images -a \
            | tail -n "+2" \
            | _egrep -i 'anime|none' \
            | awk '{ print $3 }'
    ); do
        docker image rm "$_dockerId"
    done

    clean "$@"
)

dockerRun() (
    declare USAGE="${FUNCNAME[0]} [OPTIONS...]
    Runs the generated \`anime-atsume\`

    Options:
        -p  |   Ports to map to the underlying Docker container's exposed port, 8080 (Default: -p=80 -p=443).
            |   Set \`-p=\` to disable port mapping.
        -c  |   Command to run instead of the default \`CMD\` Java command (e.g. \`/bin/bash\`).
        -h  |   Print this help message and exit.
    "
    declare _dockerRunPorts=()
    declare _dockerRunPortsDisabled=
    declare _dockerRunCmd=
    declare _dockerUnderlyingExposedPort=8080
    declare opt=
    declare OPTIND=1

    declare _dockerRunEnvArgs=()
    declare _dockerRunEnvArg=

    while getopts ":p:c:-:h" opt; do
        # Delete any leading `=` from the option argument value
        # so that e.g. `-p=80` --> `opt: p, OPTARG: 80` instead of `OPTARG: =80`
        OPTARG="${OPTARG#=}"

        case "$opt" in
            p)
                if [[ -z "$_dockerRunPortsDisabled" ]]; then
                    _dockerRunPorts+=("$OPTARG")
                fi

                if [[ -z "$OPTARG" ]]; then
                    # Clear ports if an empty port was specifically desired
                    _dockerRunPorts=()
                    _dockerRunPortsDisabled=true
                fi
                ;;
            c)
                _dockerRunCmd="$OPTARG"
                ;;
            h)
                echo -e "$USAGE"
                return 1
                ;;
            -)
                # If the arg starts with a hyphen, then it's a flag to be passed to
                # the `docker run` command, e.g. `--rm`.
                # Like other letters in `getopts -> case`, the first hyphen is removed
                # and the second hyphen is parsed into `opt`.
                # Thus, add both hyphens back in here.
                _dockerRunEnvArgs+=("--$OPTARG")
                ;;
            *)
                :  # Unknown flag/arg - Forward to underlying command
                ;;
        esac
    done

    shift $(( OPTIND - 1 ))


    if [[ -z "$_dockerRunPortsDisabled" ]]; then
        if (( ${#_dockerRunPorts[@]} == 0 )); then
            # Default ports to expose: HTTP (80) and HTTPS (443)
            _dockerRunPorts=(80 443)
        fi

        # Generates `docker run` args for port mapping, i.e. `-p hostPort:containerPort`
        _dockerRunEnvArgs+=($(printf -- "-p %s:$_dockerUnderlyingExposedPort " "${_dockerRunPorts[@]}"))
    fi

    for _dockerRunEnvArg in "$@"; do
        if [[ -z "$_dockerRunEnvArg" ]]; then
            continue
        fi

        if echo "$_dockerRunEnvArg" | _egrep -vq '^-'; then
            # If the arg passed doesn't start with a hyphen, then it's an env
            # var to be passed to the container via `-e <arg>`.
            _dockerRunEnvArgs+=("-e")
        fi

        _dockerRunEnvArgs+=("$_dockerRunEnvArg")
    done

    docker run -it "${_dockerRunEnvArgs[@]}" anime-atsume ${_dockerRunCmd}
)

dockerRunExisting() (
    docker exec -it "$(dockerGetRunningContainer)" bash
)

dockerRunOnProd() (
    declare serverKeystorePassword="$1"

    declare javaOpts=

    if [[ -f "$rootDir/keystore.p12" ]]; then
        # Ensure keystore file exists before trying to load it in the app
        javaOpts="-Dserver.ssl.key-store=${rootDir}/keystore.p12 -Dserver.ssl.key-store-password=${serverKeystorePassword}"
    fi

    declare javaOptsFlagForDockerContainer=

    if [[ -n "$javaOpts" ]]; then
        javaOptsFlagForDockerContainer="-e 'JAVA_OPTS=${javaOptsFlagForDockerContainer}'"
    fi

    # Don't kill the created docker process upon ssh exit via \`nohup\`.
    #   This primarily disconnects the TTY (interactive part of the shell
    #   that processes which interact with \`/dev/tty\`, which can handle
    #   input and output regardless of how STDIN/OUT/ERR was redirected),
    #   which means signals generated from IO, e.g. Ctrl+C, no longer
    #   affect it.
    #   See: https://superuser.com/questions/1557557/how-why-does-ssh-output-to-tty-when-both-stdout-and-stderr-are-redirected/1557724#1557724
    # It's also common to send the \`nohup CMD\` process to the background via \`&\`
    # so that it more thoroughly blocks user interaction, ensuring that the process
    # will still run after shell exit.
    #   However, in our case, we're verifying the grandchild (\`CMD\` within Docker
    #   container, e.g. source code error) doesn't error out, nor does the child
    #   (\`docker run\`, e.g. if the ports are already in use).
    #   To do so, we don't want to send the command to the background because then
    #   we'd have to manually poll \`jobs -l\`, \`ps\`, etc. output to check the status.
    #   Instead, since we want to check the success of both Docker and its
    #   underlying CMD, we use our own custom polling script logic.
    # Run Docker in 'detached mode' (similar to background but in Docker rather than Bash)
    # via \`-d\` to remove the \`CMD\` command's STDOUT during boot.
    nohup docker run \
        -d \
        -p 80:8080 \
        -p 443:8443 \
        -v ${rootDir}:/home/repo \
        ${javaOptsFlagForDockerContainer} \
        anime-atsume
)

dockerGetRunningContainer() (
    declare dockerContainerName="${1:-anime-atsume}"
    declare dockerContainerFormat="${2:-{{.Names\}\}}"

    # `--format` could also contain `{{.ID}}` (same as just `-q` flag), `{{.Image}}`, etc.
    # `--filter "status=running"` is unnecessary since `docker ps` defaults to only running containers
    docker ps \
        --filter "ancestor=${dockerContainerName}" \
        --format "$dockerContainerFormat"

    # Manual alternative:
    # docker ps --format='{{.Image}} {{.Names}}' \
    #     | _egrep "$dockerContainerName|" \
    #     | awk '{ print $2 }'
)

dockerGetLog() (
    declare dockerContainerName="${1:-$(dockerGetRunningContainer)}"

    docker logs "$dockerContainerName"
)

dockerGetLogLive() (
    docker logs --follow $(dockerGetRunningContainer)
)

dockerGetExitCode() (
    # Exit code will almost always be 0 or 1 if underlying app success/failure,
    # but will be > 1 if Docker failure (image doesn't exist, ports in use, etc.)
    declare dockerContainerName="${1:-$(dockerGetRunningContainer)}"

    docker inspect "$dockerContainerName" --format='{{.State.ExitCode}}'
)

dockerVerifyAppIsRunning() (
    declare dockerContainerName="$(dockerGetRunningContainer)"

    if [[ -z "$dockerContainerName" ]]; then
        return 1
    fi

    declare springBootPortOpenedLogMessage="Starting ProtocolHandler"
    # On average, it seems to take around 10 seconds for Spring to start
    # so keep checking for twice that time
    declare _verifyMaxTries=20
    declare _verifyCounter=0

    # Check if the container died or if it got hung up on something.
    # Specify the container name in case it died and isn't running anymore.
    while ! dockerGetLog "$dockerContainerName" 2>/dev/null | grep -q "$springBootPortOpenedLogMessage"; do
        if [[ -z "$(dockerGetRunningContainer)" ]] || (( $_verifyCounter >= $_verifyMaxTries )); then
            # Print the container's log to the console in case it was run
            # in Docker's detached mode or in Bash's background
            dockerGetLog "$dockerContainerName"
            return $(dockerGetExitCode "$dockerContainerName")
        fi

        (( _verifyCounter++ ))

        sleep 1
    done

    sleep 5

    # Add one final check in case a runtime error is encountered after the app
    # officially boots/opens its ports
    if [[ -z "$(dockerGetRunningContainer)" ]]; then
        dockerGetLog "$dockerContainerName"
        return $(dockerGetExitCode "$dockerContainerName")
    fi
)

dockerPullLatest() (
    declare dockerImageUrl="${1:-$(dockerRegistryUrl)}"
    # Extract only the last, trailing word after the final `/`
    declare dockerImageName="$(echo "$dockerImageUrl" | sed -E 's|.*/([^/]+)$|\1|')"
    # Prioritize arg 2 > parsed image name from arg 1 > anime-atsume
    dockerImageName="${dockerImageName:-anime-atsume}"
    dockerImageName="${2:-${dockerImageName}}"
    declare dockerImageNameRemote="${dockerImageUrl}:latest"

    docker pull "$dockerImageNameRemote"

    docker tag "$dockerImageUrl" "$dockerImageName"
)

dockerStopAllContainers() (
    docker stop $(docker container ls -q) 2>/dev/null
)

dockerDeleteAllContainers() (
    docker rm --volumes $(docker ps -a | tail -n +2 | awk '{ print $1 }')
)

dockerDeleteAllImages() (
    docker image rm -f $(docker images -aq)
)

dockerFindLatestImageTagFromGitLog() (
    # Gets available Docker image tags and compares them to the git log
    # to get the image corresponding to the latest commit.
    #
    # NOTE: Assumes published Docker images are tagged with the git SHA of
    # the commit which they were created from.
    declare dockerImageUrl="${1:-$(dockerRegistryUrl)}"

    # git log format: https://mirrors.edge.kernel.org/pub/software/scm/git/docs/git-show.html#_pretty_formats
    #   %H = Full commit hash
    #   %ct = Committer date UNIX timestamp (date of commit, including after rebases)
    #   %at = Author date UNIX timestamp (date of original commit, before rebases)
    #
    # skopeo is a util for getting info about Docker images, usually from remote registries.
    #
    # GitHub doesn't have a clean HTTP API for its Docker registry currently, see:
    #   https://github.com/orgs/community/discussions/26279#discussioncomment-3251171
    #   https://github.com/orgs/community/discussions/26299
    #
    # To do this:
    # 1. Get the git log commit hashes (since our Docker images are tagged
    #    with commit full-SHAs) and committer dates (since images are generated from
    #    the commit after a rebase).
    # 2. Sort by most recent commit date.
    # 3.a Get info about all Docker images available.
    # 3.b Extract their tags.
    # 3.c Remove `latest` tag (since it's not associated with a commit hash in the skopeo output).
    # 3.d Minor formatting to remove leading/trailing square brackets and whitespace.
    # 3.e Replace space delimiters between image tags to `|` for regex search.
    # 4. Print the first matching commit SHA (on the first line) since it's the most recent.
    git log --format='%H %ct' \
    | sort -rk 2 \
    | _egrep -i "$(
        docker run --rm \
            quay.io/skopeo/stable:latest \
            inspect \
            --format='{{.RepoTags}}' \
            docker://$(dockerRegistryUrl) \
        | sed -E 's/\[|\]|latest//g; s/^ | $//g' \
        | sed -E 's/ /|/g'
    )" \
    | awk '{ if (NR == 1) { print $1; } }'
)



deploy() (
    echo -e "TODO: Update this with most recent hosting platform's procedure"
)


deployAws() (
    # Assume we either have the AWS server file or that it is stored in an env var
    # declare sshLoginKeyFile="./aws-ssh-key.pem"
    #
    # if ! [[ -f "$sshLoginKeyFile" ]]; then
    #     sshLoginKeyFile="$SSH_KEY"
    # fi

    deployServerSsh "$@"
)


deployServerSsh() (
    declare USAGE="${FUNCNAME[0]} -u <username> -h <url> -k <keyfile> [OPTIONS...]
    Runs the generated \`anime-atsume\`

    Required options:
        -h  |   SSH host URL.
        -k  |   SSH login \`.pem\` key file path.

    Optional options:
        -u  |   SSH username (default: user).
        -r  |   Repository path (default: <auto-generated>).
        -d  |   Docker image registry/path URL (default: ghcr.io/<repo-path>).
        -p  |   SSL/TLS password for server's keystore (default: <blank>).
    "
    declare sshUser=
    declare sshHost=
    declare sshLoginKeyFile=
    declare repoPath=
    declare dockerImageUrl=
    declare serverKeystorePassword=

    declare opt=
    declare OPTIND=1

    while getopts ":u:h:k:r:d:p:" opt; do
        # Delete any leading `=` from the option argument value
        # so that e.g. `-p=80` --> `opt: p, OPTARG: 80` instead of `OPTARG: =80`
        OPTARG="${OPTARG#=}"

        case "$opt" in
            u)
                sshUser="$OPTARG"
                ;;
            h)
                sshHost="$OPTARG"
                ;;
            k)
                sshLoginKeyFile="$OPTARG"
                ;;
            r)
                repoPath="$OPTARG"
                ;;
            d)
                dockerImageUrl="$OPTARG"
                ;;
            p)
                serverKeystorePassword="$OPTARG"
                ;;
            *)
                echo -e "$USAGE" >&2
                return 1
                ;;
        esac
    done

    shift $(( OPTIND - 1 ))

    # Default args
    sshUser="${sshUser:-user}"
    repoPath="${repoPath:-$(repoOwnerAndName)}"
    dockerImageUrl="${dockerImageUrl:-$(dockerRegistryUrl)}"

    if [[ -z "$sshHost" ]] || [[ -z "$sshLoginKeyFile" ]]; then
        echo -e "$USAGE" >&2
        return 1
    fi



    chmod 400 "$sshLoginKeyFile"

    # SSH into the server.
    # Use the supplied .pem key via `-i` for security.
    # Auto-allow new hosts via `StrictHostKeyChecking` (simplifies logic here because we don't need to add ~/.ssh/known_hosts).
    #   Note: Not as secure as manually adding the host: https://askubuntu.com/questions/123072/ssh-automatically-accept-keys/123080#123080
    # Make ssh error if the underlying commands fail via `ExitOnForwardFailure`.
    #   Note: Could also send ssh itself to the background via -f, but this is unnecessary for CI/CD pipeline logic.
    ssh \
        -i "$sshLoginKeyFile" \
        -oStrictHostKeyChecking=accept-new \
        -oExitOnForwardFailure=yes \
        "${sshUser}@${sshHost}" \
    "
        # Allows for background/foreground job control, e.g. bg, fg, jobs -l, etc.
        set -m

        [[ -d anime-atsume ]] && rm -rf anime-atsume
        git clone https://github.com/${repoPath}.git

        # Stay in the cloned repo's dir to gain access to our helpful index.sh commands.
        #   Note: This means the server's volume mounted to Docker needs to be relative
        #   to the repo dir, not the \`WORKDIR\` or \`HOME\`.
        cd anime-atsume

        ./index.sh dockerStopAllContainers
        ./index.sh dockerDeleteAllContainers
        ./index.sh dockerDeleteAllImages
        ./index.sh dockerPullLatest ${dockerImageUrl}
        ./index.sh certConvertToPkcs
        ./index.sh certRenewSetCronJob ${serverKeystorePassword}

        ./index.sh dockerRunOnProd ${serverKeystorePassword}

        # Before starting our potentially time-consuming polling, ensure the
        # \`docker run\` command itself didn't fail (e.g. if ports were already in
        # use or couldn't find the image) by checking the exit code of \`nohup CMD\`.
        dockerRunExitCode=\"\$?\"
        (( \$dockerRunExitCode > 0 )) && exit \$dockerRunExitCode

        # Verify the app is running correctly and forward the exit code accordingly
        ./index.sh dockerVerifyAppIsRunning
        exit \$?
    "
)


deployRenderIO() (
    # See:
    #   - https://render.com/docs/deploy-a-commit#deploying-a-commit-via-webhook
    #   - https://api-docs.render.com/reference/create-deploy
    declare _renderDotComServiceId="${_renderDotComServiceId}"
    declare _renderDotComApiKey="${_renderDotComApiKey}"

    # Only attempt building Docker image if `docker` exists
    if command -v docker &>/dev/null; then
        dockerClean
        dockerBuild "$@"
    fi

    curl \
         --url "https://api.render.com/v1/services/${_renderDotComServiceId}/deploys" \
         --request 'POST' \
         --header 'Accept: application/json' \
         --header 'Content-Type: application/json' \
         --header "Authorization: Bearer ${_renderDotComApiKey}"
)


certbot() (
    docker run -it --rm \
        --name certbot \
        -v "/etc/letsencrypt:/etc/letsencrypt" \
        -v "/var/lib/letsencrypt:/var/lib/letsencrypt" \
        -p 80:80 \
        -p 443:443 \
        certbot/certbot \
        "$@"
)

certGenerate() (
    certbot certonly --standalone
)

certConvertToPkcs() (
    # Spring Boot expects PKCS#12 format, not .pem, so generate it here.

    declare origUser="$(whoami)"

    declare pemParentPath="${1:-/etc/letsencrypt}"
    # Specifically get only symlinks via `-type l`
    declare pemAllFilesRel=($(sudo find "$pemParentPath" -iname '*.pem' -type l))
    declare pemFirstFile="${pemAllFilesRel[0]}"
    declare pemParentPathAbs="$(dirname $(sudo realpath -e "$pemFirstFile"))"
    declare pemParentPathRel="$(dirname $(sudo realpath -se "$pemFirstFile"))"
    # Exclude symlinks via `-type f`
    declare pemAllFilesAbs=($(sudo find "$pemParentPathAbs" -iname '*.pem' -type f))

    declare opensslInFile="${pemParentPathAbs}/$(echo ${pemAllFilesAbs[@]} | _egrep -io 'fullchain[^\s]*\.pem')"
    declare opensslInKeyFile="${pemParentPathAbs}/$(echo ${pemAllFilesAbs[@]} | _egrep -io 'privkey[^\s]*\.pem')"
    declare opensslInCaFile="${pemParentPathAbs}/$(echo ${pemAllFilesAbs[@]} | _egrep -io '\bchain[^\s]*\.pem')"

    declare pkcsOutFilename="keystore.p12"
    declare pkcsOutFileAbs="$pemParentPathAbs/$pkcsOutFilename"
    declare pkcsOutFileRel="$pemParentPathRel/$pkcsOutFilename"
    declare pkcsOutFileHome="$HOME/$pkcsOutFilename"
    declare pkcsOutFileApp="$rootDir/$pkcsOutFilename"

    # The file had already been created so no need to recreate it from scratch.
    # Just make sure it's in both HOME and app dirs.
    if [[ -f "$pkcsOutFileApp" ]] || [[ -f "$pkcsOutFileHome" ]]; then
        if ! [[ -f "$pkcsOutFileApp" ]]; then
            sudo cp "$pkcsOutFileHome" "$pkcsOutFileApp"
        fi

        if ! [[ -f "$pkcsOutFileHome" ]]; then
            sudo cp "$pkcsOutFileApp" "$pkcsOutFileHome"
        fi

        return
    fi

    sudo rm -f "$pkcsOutFilename" "$pkcsOutFileAbs" "$pkcsOutFileRel"

    # See:
    #   - Generating PKCS#12 from LetsEncrypt .pem files: https://stackoverflow.com/a/38873138/5771107
    #   - Password inline in command: https://stackoverflow.com/a/27497899/5771107
    #
    # Generates a command akin to:
    #
    # openssl pkcs12 -export \
    #     -in fullchain.pem \
    #     -inkey privkey.pem \
    #     -CAfile chain.pem \
    #     -caname root \
    #     -name tomcat \
    #     -out keystore.p12
    sudo openssl pkcs12 -export \
        -in "$opensslInFile" \
        -inkey "$opensslInKeyFile" \
        -CAfile "$opensslInCaFile" \
        -caname root \
        -name tomcat \
        -passout pass: \
        -out "$pkcsOutFileAbs"

    # It'd probably be a good idea not to change ownership of the file
    # to prevent accidental modifications to it.
    #sudo chown "$origUser:$origUser" "$pkcsOutFileAbs"
    # Allow everyone to read the keystore file.
    sudo chmod a+r "$pkcsOutFileAbs"

    # Make a symlink to the keystore file in the `/etc/letsencrypt/live/` dir
    # where all the other `.pem` files are.
    # I don't think this is necessary but it doesn't hurt to do so.
    sudo ln -s "$pkcsOutFileAbs" "$pkcsOutFileRel"
    # However, don't make a symlink in the current dir, rather copy the file
    # because the parent dirs of the keystore are only readable by root.
    sudo cp "$pkcsOutFileAbs" "$pkcsOutFileHome"
    sudo chmod a+r "$pkcsOutFileHome"
    sudo cp "$pkcsOutFileHome" "$pkcsOutFileApp"

    echo "$pkcsOutFileAbs"
)

certCopyToDockerContainer() (
    # See:
    #   - https://stackoverflow.com/questions/68971170/how-to-add-files-to-an-existing-docker-image/68971285#68971285
    docker cp "$(certConvertToPkcs)" anime-atsume:/home
)

certRenewSetCronJob() (
    # Don't create a duplicate cronjob if one already exists.
    # Assume one exists if 'index.sh' is called within it.
    if cat /etc/crontab | grep -q 'index.sh'; then
        return
    fi

    declare serverKeystorePassword="$1"

    # See: https://eff-certbot.readthedocs.io/en/stable/using.html#setting-up-automated-renewal
    declare _renewDelay=$(
        awk '
            BEGIN {
                srand();
                print int(rand()*(3600+1))
            }
        '
    )

    echo "0 0,12 * * * root sleep $_renewDelay && ( ./index.sh dockerStopAllContainers; ./index.sh certbot renew -q; ./index.sh certConvertToPkcs; ./index.sh dockerRunOnProd ${serverKeystorePassword}; )" \
        | sudo tee -a /etc/crontab > /dev/null
)



mountServerLocally() {
    declare USAGE="${FUNCNAME[0]} -h <url> [OPTIONS...]
    Runs the generated \`anime-atsume\`

    Required options:
        -h  |   SSH host URL.

    Optional options:
        -u  |   SSH username (default: user).
        -k  |   SSH login \`.pem\` key file path.
        -s  |   Server's path to mount (default: /).
        -l  |   Local path to mount (default: \$HOME/server).
    "
    declare sshUser=
    declare sshHost=
    declare sshLoginKeyFile=
    declare serverMountPath=
    declare localMountPath=

    declare opt=
    declare OPTIND=1

    while getopts ":u:h:k:s:l:" opt; do
        # Delete any leading `=` from the option argument value
        # so that e.g. `-p=80` --> `opt: p, OPTARG: 80` instead of `OPTARG: =80`
        OPTARG="${OPTARG#=}"

        case "$opt" in
            u)
                sshUser="$OPTARG"
                ;;
            h)
                sshHost="$OPTARG"
                ;;
            k)
                sshLoginKeyFile="$OPTARG"
                ;;
            s)
                serverMountPath="$OPTARG"
                ;;
            l)
                localMountPath="$OPTARG"
                ;;
            *)
                echo -e "$USAGE" >&2
                return 1
                ;;
        esac
    done

    shift $(( OPTIND - 1 ))

    sshUser="${sshUser:-user}"
    serverMountPath="${serverMountPath:-/}"
    localMountPath="${localMountPath:-$HOME/server}"

    if ! [[ -d "$localMountPath" ]]; then
        mkdir "$localMountPath"
    fi

    # See:
    #   - sshfs with .pem file: https://stackoverflow.com/questions/22217767/how-to-mount-a-folder-on-amazon-ec2-instance-with-private-key-using-sshfs/22249600#22249600
    declare optionalSshfsOpts=

    if [[ -n "$sshLoginKeyFile" ]]; then
        optionalSshfsOpts="-o IdentityFile=$(realpath "$sshLoginKeyFile")"
    fi

    sshfs "${sshUser}@${sshHost}:${serverMountPath}" "$localMountPath" $optionalSshfsOpts

    echo "$localMountPath"
}

unmountServerLocally() {
    sudo umount -f "$1"
}


mountAwsLocally() {
    mountServerLocally \
        -u ubuntu \
        -h "$1" \
        -l "$HOME/aws-server" \
        -k "${2:-./aws-ssh-key.pem}"
}

unmountAwsLocally() {
    unmountServerLocally "$HOME/aws-server"
}


awsGetInstancesUrls() {
    # Specify instance ID if present
    declare awsInstanceId="$1"

    # For some reason, `aws` can't process strings as options, and
    # especially strings with quotes in them.
    # Thus, use an array.
    declare awsCmdOpts=()

    if [[ -n "$awsInstanceId" ]]; then
        awsCmdOpts+=(--filters Name=instance-id,Values=${awsInstanceId})
    fi

    aws ec2 describe-instances \
        --query 'Reservations[*].Instances[*].PublicDnsName' \
        --filter 'Name=instance-state-name,Values=running' \
        --output text \
        ${awsCmdOpts[@]}
}

awsGetInstancesIds() {
    # Specify instance URL if present
    declare awsInstanceUrl="$1"

    # For some reason, `aws` can't process strings as options, and
    # especially strings with quotes in them.
    # Thus, use an array.
    declare awsCmdOpts=()

    if [[ -n "$awsInstanceUrl" ]]; then
        awsCmdOpts+=(--filters Name=dns-name,Values=${awsInstanceUrl})
    fi

    aws ec2 describe-instances \
        --query 'Reservations[*].Instances[*].InstanceId' \
        --filter 'Name=instance-state-name,Values=running' \
        --output text \
        ${awsCmdOpts[@]}
}

awsSsh() {
    declare sshHost="${1:-$(awsGetInstancesUrls)}"
    declare sshUser="${2:-ubuntu}"

    ssh \
        -i "./aws-ssh-key.pem" \
        -oStrictHostKeyChecking=accept-new \
        -oExitOnForwardFailure=yes \
        "${sshUser}@${sshHost}"
}



main() {
    declare USAGE="${BASH_SOURCE[0]} [OPTIONS...] <cmd> [CMD_OPTIONS...]
    Run commands for both Anime Atsume's client and server directories.

    Options:
        -i  |   Install build-script dependencies if not present (e.g. \`nvm\`).
        -h  |   Print this help message and exit.

    Commands:
$(
    declare -f \
        | _egrep -i '^[^_][^ \t]+ \(\)' \
        | _egrep -iv 'main' \
        | sed -E 's/ \(\)//' \
        | sed -E 's/^/        /'
)
    "

    declare OPTIND=1

    while getopts ":ih" opt; do
        case "$opt" in
            h)
                if (( $# == 1 )); then
                    # User ran `thisFile -h` rather than `thisFile command -h`
                    # so print `main()` help message.
                    # The latter format's command should handle flags itself.
                    echo -e "$USAGE"
                    return 1
                fi
                ;;
            i)
                "${rootDir}/nvm-install.sh"
                ;;
            *)
                :  # Unknown flag - Forward to desired command
                ;;
        esac
    done

    shift $(( OPTIND - 1 ))

    declare cmd="$1"  # First arg = command to run. Last arg = `${@:$#}``
    declare cmdArgs=("${@:2}")

    if [[ -z "$cmd" ]]; then
        main -h

        return 1
    fi

    $cmd "${cmdArgs[@]}"
}


main "$@"
