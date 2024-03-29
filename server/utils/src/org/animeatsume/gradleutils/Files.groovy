import org.gradle.api.tasks.SourceSet;


def downloadFile(String filePath, String url) {
    return downloadFile(new File(filePath), url);
}
def downloadFile(File file, String url) {
    println("Downloading file \"${file.name}\" from <${url}>...");

    new URL(url).withInputStream { inStream ->
        file.withOutputStream {
            it << inStream
        }
    }

    println("Downloaded file \"${file.name}\" from <${url}>.");
}


def getSourceFiles() {
    return getSourceFiles(true);
}
def getSourceFiles(boolean includeSourceSetNames) {
    return getSourceFiles(null, includeSourceSetNames);
}
def getSourceFiles(SourceSet sourceSetsToInspect) {
    return getSourceFiles(sourceSetsToInspect, true);
}
def getSourceFiles(SourceSet sourceSetsToInspect, boolean includeSourceSetNames) {
    StringBuilder sb = new StringBuilder();

    sourceSetsToInspect.each {
        if (includeSourceSetNames) {
            sb.append("${it}\n");
        }

        it.allSource.each {
            // file path/name
            sb.append("${it}\n");
        }
    }

    return sb.toString().trim();
}


def searchFile(Map<String, Closure> places, List<String> searchPaths, String searchID) {
    File result = null;

    places.each { keyDescription, filePathMaybe ->
        if (result != null) {
            return;
        }

        project.logger.debug("Looking for $searchID in $keyDescription");

        def dir = filePathMaybe();

        if (dir == null) {
            project.logger.debug("$keyDescription not set");
        } else {
            project.logger.debug("$keyDescription is $dir");

            searchPaths.each { parentDirOfFileMaybePath ->
                if (result != null) {
                    return;
                }

                File parentDirOfFileMaybe = new File(dir, parentDirOfFileMaybePath);

                project.logger.debug("Trying $parentDirOfFileMaybe.path");

                if (parentDirOfFileMaybe.exists() && parentDirOfFileMaybe.file) {
                    project.logger.debug("found $searchID as $result");
                    result = parentDirOfFileMaybe;
                }
            }
        }
    }

    if (!result?.file) {
        throw new FileNotFoundException("Could not find $searchID, please set one of ${places.keySet()}");
    } else {
        project.logger.info("$searchID: ${result}");

        return result
    }
}


def copyDbToBuildDir() {
    copy {
        from dbName
        into warOutputDir
    }

    println("Copied ${dbName} to ${warOutputDir}")
}


tasks.register("cleanResources") {
    description "Deletes the server's web asset directory (${gradle.rootProject.serverWebAssetDirPath}).";

    doLast {
        delete(gradle.rootProject.serverWebAssetDirPath);
    }
}
tasks.register("cleanAll") {
    description "Deletes all build directories and resources.";

    dependsOn("clean", "cleanResources");
}

tasks.register("printSrcInfo") {
    description "Prints project source code files (-Ps for long list).";

    doLast {
        println("project.files:\n${objToJson(project.files("src", "buildSrc"))}\n\n");
        println("files:\n${objToJson(files("src", "buildSrc"))}\n\n");

        // Note: `rootProject.artifacts` has changed over the years, so get the source code files via `sourceSets`
        if (project.hasProperty("s") || System.hasProperty("s")) {
            // Show source code files, but only upon request b/c it can easily be a ridiculously long list
            println(getSourceFiles());
            println();
        }
    }
}

tasks.register("printCommands") {
    description "Prints all Gradle commands.";

    /**
     * Alternatives:
     *  - {@code configurations.forEach(config -> { println(config.name); });}
     *  - {@code ./gradlew --console plain dependencies 2>/dev/null | grep -Eio '^[a-z]+ - .*'}
     *
     * Related:
     *  - {@code ./gradlew -q help --task <myTask>}
     *
     * @see <a href="https://stackoverflow.com/questions/38362977/how-to-list-all-tasks-for-the-master-project-only-in-gradle/40188539#40188539">List all Gradle tasks</a>
     * @see <a href="https://stackoverflow.com/questions/41173616/how-can-i-get-a-list-of-all-configurations-for-a-gradle-project">List all Gradle configurations</a>
     */
    doLast {
        String cmd = "./gradlew -q tasks --all";

        println("Running `${cmd}`...");
        println(cliCmd("${cmd}"));
    }
}

tasks.register("printSrcFiles") {
    description "Prints project source code files.";

    doLast {
        println(getSourceFiles());
    }
}

/**
 * @see <a href="https://docs.gradle.org/current/userguide/userguide_single.html#sec:listing_dependencies">Gradle docs</a>
 * @see <a href="https://stackoverflow.com/questions/21645071/using-gradle-to-find-dependency-tree">SO: Listing Gradle dependency tree</a>
 */
tasks.register("printDependencies") {
    description "Prints all project dependencies.";

    doLast {
        cliCmd("./gradlew dependencies");
    }
}

// TODO: https://stackoverflow.com/questions/50401813/how-to-find-type-of-gradle-task


ext {
    downloadFile = this.&downloadFile;
    getSourceFiles = this.&getSourceFiles;
    searchFile = this.&searchFile;
    copyDbToBuildDir = this.&copyDbToBuildDir;
}

