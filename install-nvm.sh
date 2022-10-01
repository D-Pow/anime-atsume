#!/usr/bin/env -S bash

declare _nvmNodeVersion="${1:---lts}"

# Install NVM

export NVM_DIR="$HOME/.nvm"
[ -s "$NVM_DIR/nvm.sh" ] && source "$NVM_DIR/nvm.sh"  # Load nvm
[ -s "$NVM_DIR/bash_completion" ] && source "$NVM_DIR/bash_completion"  # Load nvm bash_completion
export NVM_SYMLINK_CURRENT=true  # Makes a symlink at ~/.nvm/current/bin/node so you don't have to change IDEs' configurations when changing node versions
export NVM_CURRENT_HOME="$NVM_DIR/current"
export PATH="$NVM_CURRENT_HOME/bin:$PATH"

# Create `.nvm/current/` dir

declare _nvmCurrentNodePath="$(find -L "$NVM_CURRENT_HOME" -iname node_modules 2>/dev/null \
    | sed -E 's|.*node_modules/.+||' \
    | uniq \
    | sed -E 's/(^\s+)|(\s+$)//g' \
    | grep -Ev '^[[:space:]]*$'
)"

export NODE_PATH="${NODE_PATH:+${NODE_PATH}:}$_nvmCurrentNodePath"

nvm install "$_nvmNodeVersion"
nvm use "$_nvmNodeVersion"
