def installNvmAndNodeJsIfNotPresent() {
    // Native Bash command: ( ( command -v 'node' || [[ -n "${!1}" ]] ) &> /dev/null; if (( $? == 0 )); then echo 'NodeJS is installed'; fi; )
    String nodeJsIsInstalledStdOut = cliCmd("node --version");
    boolean nodeJsIsInstalled = nodeJsIsInstalledStdOut != null && nodeJsIsInstalledStdOut.length() > 0;

    if (!nodeJsIsInstalled) {
        println "Installing NodeJS version ${nodeVersion} using `nvm` via `${repoRootDirPath}/nvm-install.sh`..."

        cliCmd("${repoRootDirPath}/nvm-install.sh ${nodeVersion}");

        println "NodeJS installation successful!"
    }
}

def installFrontendDeps() {
    File clientDirDeps = new File(clientDirDepsPath);  // Defined in build.gradle
    boolean clientDepsInstalled = clientDirDeps.exists() && clientDirDeps.list().size() > 0;

    if (!clientDepsInstalled) {
        println("Client-side dependencies directory ${clientDirDepsPath} doesn't exist. Running front-end install now...");

        // Alternative (if async were required): `gradle.taskGraph.afterTask { Task task, TaskState state -> {}}`
        // See:
        //     - Change dir for CLI command: https://discuss.gradle.org/t/change-gradle-working-directory-in-a-standard-operating-system-way-where-gradle-is-launched-and-not-where-the-build-gradle-is-located/7153/10
        String npmInstallStdOut = cliCmd("npm install", "${clientDirPath}");

        println("${npmInstallStdOut}\n");
        println("Front-end install successful!");
    }
}

def buildFrontendIfNotPresent() {
    def serverWebAssetDir = new File(serverWebAssetDirPath);

    if (serverWebAssetDir.exists() && serverWebAssetDir.list().size() > 0) {
        return;
    }

    println("Client build output directory ${serverWebAssetDirPath} doesn't exist. Building the front-end now...");

    installNvmAndNodeJsIfNotPresent();
    installFrontendDeps();

    String npmBuildStdOut = cliCmd("npm run build", "${clientDirPath}");;

    println("${npmBuildStdOut}\n");
    println("Front-end build completed successfully!");
}


ext {
    installNvmAndNodeJsIfNotPresent = this.&installNvmAndNodeJsIfNotPresent;
    installFrontendDeps = this.&installFrontendDeps;
    buildFrontendIfNotPresent = this.&buildFrontendIfNotPresent;
}
