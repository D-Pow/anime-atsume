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
    declare dockerImageName="${1:-anime-atsume}"
    declare dockerContainerName="$(dockerGetRunningContainer)"

    docker logs "$dockerContainerName"
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

dockerFindLatestImageTagFromGitLog() (
    # Gets available Docker image tags and compares them to the git log
    # to get the image corresponding to the latest commit.
    #
    # NOTE: Assumes published Docker images are tagged with the git SHA of
    # the commit which they were created from.
    declare dockerImageUrl="${1:-ghcr.io/d-pow/anime-atsume}"

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
            docker://ghcr.io/d-pow/anime-atsume \
        | sed -E 's/\[|\]|latest//g; s/^ | $//g' \
        | sed -E 's/ /|/g'
    )" \
    | awk '{ if (NR == 1) { print $1; } }'
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
    if [[ -f "$pkcsOutFilename" ]] && [[ -f "$pkcsOutFileAbs" ]] && [[ -f "$pkcsOutFileRel" ]]; then
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
    sudo chmod a+r "$pkcsOutFilename"
    sudo cp "$pkcsOutFilename" "$HOME/$pkcsOutFilename"

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
