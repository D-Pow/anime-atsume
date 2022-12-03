#!/usr/bin/env bash

declare rootDir="$(realpath -se "$(dirname "${BASH_SOURCE[@]}")")"
declare clientDir="${rootDir}/client"
declare serverDir="${rootDir}/server"
declare buildDir="${serverDir}/build/libs"


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
    cd "${serverDir}"

    declare _buildCleanFirst=
    declare _buildVerbose=
    declare OPTIND=1

    while getopts ":cvh" opt; do
        case "$opt" in
            c)
                _buildCleanFirst=true
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

    declare _buildGradleOpts="${_buildCleanFirst:+clean} ${_buildVerbose:+--console plain}"

    ./gradlew ${_buildGradleOpts} build
)


checkBuildOutputDir() (
    cd "${serverDir}"

    if ! [[ -d "${buildDir}" ]]; then
        clean
        build "$@"
    fi
)


dockerBuild() (
    declare _dockerBuildVerbose=
    declare OPTIND=1

    while getopts ":v" opt; do
        case "$opt" in
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

    docker build ${_dockerBuildVerbose:+--progress=plain} -t anime-atsume $@ .
)

dockerRemoveOldContainers() (
    declare _dockerImageId=

    for _dockerImageId in $(
        docker images -a \
            | tail -n "+2" \
            | awk '{ print $3 }'
    ); do
        docker image rm "$_dockerImageId"
    done
)

dockerRun() (
    docker run -it --rm anime-atsume bash
)


deploy() (
    clean "$@"
    build "$@"
    dockerBuild "$@"

    # See:
    #   - https://render.com/docs/deploy-a-commit#deploying-a-commit-via-webhook
    #   - https://api-docs.render.com/reference/create-deploy
    declare _renderDotComServiceId="${_renderDotComServiceId}"
    declare _renderDotComApiKey="${_renderDotComApiKey}"

    curl \
         --url "https://api.render.com/v1/services/${_renderDotComServiceId}/deploys" \
         --request 'POST' \
         --header 'Accept: application/json' \
         --header 'Content-Type: application/json' \
         --header "Authorization: Bearer ${_renderDotComApiKey}"
)


main() {
    declare USAGE="${rootDir}/${BASH_SOURCE[0]} [OPTIONS...] <cmd>
    Run commands for both Anime Atsume's client and server directories.

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

    while getopts ":h" opt; do
        case "$opt" in
            h)
                echo -e "$USAGE"
                return 1
                ;;
            *)
                :  # Unknown flag - Forward to desired command
                ;;
        esac
    done

    shift $(( OPTIND - 1 ))

    declare cmd="$1"  # First arg = command to run. Last arg = `${@:$#}``
    declare cmdArgs="${@:2}"

    $cmd "$cmdArgs"
}


main "$@"
