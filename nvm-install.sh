#!/usr/bin/env -S bash


if command -v node &>/dev/null; then
    exit
fi

# Use LatestStableVersion of NodeJS if a version isn't specified

declare _nvmNodeVersion="${1:---lts}"


# Use extended regex if possible (should be if on "true" Unix system, i.e. not Mac)

egrep() {
    declare _egrepCommandFlag='-P';
    declare perlRegexSupported="$(echo 'true' | grep -P 'u' 2>/dev/null)";

    if [[ -z "$perlRegexSupported" ]]; then
        _egrepCommandFlag='-E';
    fi;

    grep --exclude-dir={node_modules,.git,.idea,lcov-report} --color=auto $_egrepCommandFlag "$@"
}


# Get latest NVM version

# Use the script from the GitHub ReadMe/docs.
#   - NVM docs: https://github.com/nvm-sh/nvm#install--update-script
# Except get the latest release version from the GitHub API
# and inject that into the NVM script URL.
#   - GH API docs (get latest release): https://docs.github.com/en/rest/releases/releases?apiVersion=2022-11-28#get-the-latest-release
declare _nvmVersionTags="$(
    curl -so- https://api.github.com/repos/stedolan/jq/releases \
        | jq -r '.[] | .tag_name' \
        | sort -Vr
)"
declare _nvmLatestVersionTag="$(
    echo -n "$_nvmVersionTags" \
        | head -n 1
)"
# Even `echo -n` doesn't remove the newline in all occasions, so simply subtract said newline
# from the output when counting number of `.` chars present in output version-text
declare _nvmNumSubVersions=$((
    "$(
        echo -n "$_nvmLatestVersionTag" \
            | egrep -o '\.' \
            | wc -m
    )" \
    - 1
))
# Replace any text with `0.`, e.g. `jq-1.6 --> 0.1.6`
declare _nvmLatestVersionDownloadTag="$(
    echo "$_nvmLatestVersionTag" \
        | sed -E 's/[^.0-9 \t\n$]+([0-9]*)/0.\1/; s/[^.0-9 \t\n$]+/0./'
)"
# The above *almost* works, except `jq` doesn't sync tags and releases.
# Thus, manually override the above with the below.
_nvmLatestVersionDownloadTag="$(
    curl --silent https://api.github.com/repos/nvm-sh/nvm/tags \
        | egrep -i "^[^\"']+[\"']name" \
        | egrep -io "v[^\"']+" \
        | sort -Vr \
        | head -n 1
)"


# Install NVM

curl -o- "https://raw.githubusercontent.com/nvm-sh/nvm/${_nvmLatestVersionDownloadTag}/install.sh" | bash


# Setup environment for NVM usage

export NVM_DIR="$([[ -n "$XDG_CONFIG_HOME" ]] && echo "$XDG_CONFIG_HOME/nvm" || echo "$HOME/.nvm")"
[ -s "$NVM_DIR/nvm.sh" ] && source "$NVM_DIR/nvm.sh"  # Load nvm
[ -s "$NVM_DIR/bash_completion" ] && source "$NVM_DIR/bash_completion"  # Load nvm bash_completion
export NVM_SYMLINK_CURRENT=true  # Makes a symlink at ~/.nvm/current/bin/node so you don't have to change IDEs' configurations when changing node versions
export NVM_CURRENT_HOME="$NVM_DIR/current"
export PATH="$NVM_CURRENT_HOME/bin:$PATH"

echo "
export NVM_DIR=\"\$([[ -n \"\$XDG_CONFIG_HOME\" ]] && echo \"\$XDG_CONFIG_HOME/nvm\" || echo \"\$HOME/.nvm\")\"
[ -s \"\$NVM_DIR/nvm.sh\" ] && source \"\$NVM_DIR/nvm.sh\"  # Load nvm
[ -s \"\$NVM_DIR/bash_completion\" ] && source \"\$NVM_DIR/bash_completion\"  # Load nvm bash_completion
export NVM_SYMLINK_CURRENT=true  # Makes a symlink at ~/.nvm/current/bin/node so you don't have to change IDEs' configurations when changing node versions
export NVM_CURRENT_HOME=\"\$NVM_DIR/current\"
export PATH=\"\$NVM_CURRENT_HOME/bin:\$PATH\"
" >> $HOME/.bashrc


# Create `.nvm/current/` dir

declare _nvmCurrentNodePath="$(find -L "$NVM_CURRENT_HOME" -iname node_modules 2>/dev/null \
    | sed -E 's|.*node_modules/.+||' \
    | uniq \
    | sed -E 's/(^\s+)|(\s+$)//g' \
    | grep -Ev '^[[:space:]]*$'
)"


# Update `PATH`

export NODE_PATH="${NODE_PATH:+${NODE_PATH}:}$_nvmCurrentNodePath"
export PATH="${NODE_PATH}:${PATH}"


nvm install "$_nvmNodeVersion"
nvm use "$_nvmNodeVersion"
