#!/usr/bin/env bash

declare rootDir="$(realpath -se "$(dirname "${BASH_SOURCE[@]}")")"
declare clientDir="${rootDir}/client"
declare serverDir="${rootDir}/server"
declare buildDir="${serverDir}/build/libs"
declare feBuildDir="$(realpath -se "${rootDir}/$(
    cat "${clientDir}/package.json" \
    | grep -E '\bbuildOutputDir' \
    | awk '{ print $2 }' \
    | sed -E 's/"//g; s|\.*/?|\./|'
)")"


_egrep() {
    declare _egrepCommandFlag='-P';
    declare perlRegexSupported="$(echo 'true' | grep -P 'u' 2>/dev/null)";

    if [[ -z "$perlRegexSupported" ]]; then
        _egrepCommandFlag='-E';
    fi;

    grep --exclude-dir={node_modules,.git,.idea,lcov-report} --color=auto $_egrepCommandFlag "$@"
}


clean() (
    cd "${clientDir}"
    npm run clean

    cd "${serverDir}"
    ./gradlew clean
)


build() (
    declare _buildCleanFirst=
    declare _buildCopyFilesToRootDir=
    declare _buildHeadless=
    declare _buildVerbose=
    declare OPTIND=1

    while getopts ":fcrvmh" opt; do
        case "$opt" in
            f)
                clean
                ;;
            c)
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

                ./gradlew printSrcInfo
                ./gradlew printSrcFiles
                ./gradlew printDependencies
                ;;
            h)
                ./gradlew printCommands

                return
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

dockerPullLatest() (
    declare dockerImageUrl="${1:-ghcr.io/d-pow/anime-atsume}"
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


deploy() (
    echo -e "TODO: Update this with most recent hosting platform's procedure"
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


certGenerate() (
    docker run -it --rm \
        --name certbot \
        -v "/etc/letsencrypt:/etc/letsencrypt" \
        -v "/var/lib/letsencrypt:/var/lib/letsencrypt" \
        -p 80:80 \
        -p 443:443 \
        certbot/certbot \
        certonly --standalone
)

certConvertToPkcs() (
    # Spring Boot expects PKCS#12 format, not .pem, so generate it here.

    declare origUser="$(whoami)"

    declare pemParentPath="${1:-/etc/letsencrypt/live}"
    declare pemAllFilesRel=($(sudo find "$pemParentPath" -iname '*.pem'))
    declare pemFirstFile="${pemAllFilesRel[0]}"
    declare pemParentPathAbs="$(dirname $(sudo realpath -e "$pemFirstFile"))"
    declare pemParentPathRel="$(dirname $(sudo realpath -se "$pemFirstFile"))"
    declare pemAllFilesAbs=($(sudo find "$pemParentPathAbs" -iname '*.pem'))

    declare opensslInFile="${pemParentPathAbs}/$(echo ${pemAllFilesAbs[@]} | _egrep -io 'fullchain[^\s]*\.pem')"
    declare opensslInKeyFile="${pemParentPathAbs}/$(echo ${pemAllFilesAbs[@]} | _egrep -io 'privkey[^\s]*\.pem')"
    declare opensslInCaFile="${pemParentPathAbs}/$(echo ${pemAllFilesAbs[@]} | _egrep -io '\bchain[^\s]*\.pem')"

    declare pkcsOutFilename="keystore.p12"
    declare pkcsOutFileAbs="$pemParentPathAbs/$pkcsOutFilename"
    declare pkcsOutFileRel="$pemParentPathRel/$pkcsOutFilename"

    # All SSL keystore files already exist, so no need to regenerate them
    if [[ -f "$pkcsOutFilename" ]] && if [[ -f "$pkcsOutFileAbs" ]] && if [[ -f "$pkcsOutFileRel" ]]; then
        return
    fi

    sudo rm -f "$pkcsOutFilename" "$pkcsOutFileAbs" "$pkcsOutFileRel"

    # See:
    #   - Generating PKCS#12 from LetsEncrypt .pem files: https://stackoverflow.com/a/38873138/5771107
    #   - Password inline in command: https://stackoverflow.com/a/27497899/5771107
    sudo openssl pkcs12 -export \
        -in "$opensslInFile" \
        -inkey "$opensslInKeyFile" \
        -out $pkcsOutFileAbs \
        -name tomcat \
        -CAfile "$opensslInCaFile" \
        -passout pass: \
        -caname root

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
    sudo cp "$pkcsOutFileAbs" "$pkcsOutFilename"
    sudo chmod a+r "$pkcsOutFileAbs"

    echo "$pkcsOutFileAbs"
)

certCopyToDockerContainer() (
    # See:
    #   - https://stackoverflow.com/questions/68971170/how-to-add-files-to-an-existing-docker-image/68971285#68971285
    docker cp "$(certConvertToPkcs)" anime-atsume:/home
)


main() {
    declare USAGE="${rootDir}/${BASH_SOURCE[0]} [OPTIONS...] <cmd>
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
                echo -e "$USAGE"
                return 1
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
