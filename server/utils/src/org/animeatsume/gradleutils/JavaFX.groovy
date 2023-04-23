def findJavaFxJar() {
    return searchFile(
        [
            'jfxrtDir in Gradle Properties': { System.properties['jfxrtDir'] },
            'JFXRT_HOME in System Environment': { System.getenv('JFXRT_HOME') },
            'JAVA_HOME in System Environment': { System.getenv('JAVA_HOME') },
            'java.home in JVM properties': { System.properties['java.home'] }
        ],
        [
            'jfxrt.jar',
            'lib/jfxrt.jar',
            'lib/ext/jfxrt.jar',
            'jre/lib/jfxrt.jar',
            'jre/lib/ext/jfxrt.jar'
        ],
        'JavaFX Runtime Jar'
    )
}

def findAntJavaFxJar() {
    return searchFile(
        [
            'jfxrtDir in Gradle Properties': { System.properties['jfxrtDir'] },
            'JFXRT_HOME in System Environment': { System.getenv('JFXRT_HOME') },
            'JAVA_HOME in System Environment': { System.getenv('JAVA_HOME') },
            'java.home in JVM properties': { System.properties['java.home'] }
        ],
        [
            'ant-javafx.jar',
            'lib/ant-javafx.jar',
            '../lib/ant-javafx.jar'
        ],
        'JavaFX Packager Tools'
    )
}

def getJavaFxJarPath() {
    return file(findJavaFxJar()).absolutePath
}


def downloadJavaFX() {
    /**
     * Download JavaFX manually because it requires OS-specific bindings that can't be packaged in a .jar
     * file (meaning we can't just add `org.openjfx` as a dependency solely, we need the .jar files from
     * the OpenJFX website)
     *
     * @see <a href="https://gluonhq.com/products/javafx">JavaFX .jar download site</a>
     * @see <a href="https://mvnrepository.com/artifact/org.openjfx">Gradle dependencies options</a>
     * @see <a href="https://wiki.openjdk.org/display/OpenJFX/Main">OpenJFX homepage</a>
     * @see <a href="https://openjfx.io/openjfx-docs">OpenJFX docs</a>
     * @see <a href="https://jdk.java.net/20">OpenJDK homepage</a>
     * @see <a href="https://stackoverflow.com/a/70175935/5771107">The "correct" way to create distributable JFX .jar - Platform/OS installer</a>
     */
    String javafxVersionMajor = javafxVersion.replaceAll("\\D.*", "");  // Only needed if using an early-access version
    boolean javafxVersionIsEarlyAccess = javafxVersion.matches(".*\\W(ea|EA)\\W.*");
    String javafxVersionDownloadParentPath = javafxVersionIsEarlyAccess ? javafxVersionMajor : javafxVersion;

    boolean javafxHeadless = Boolean.parseBoolean(System.getProperty("headless", "false"));
    String javafxVersionSuffix = javafxHeadless
        ? "monocle-" // Monocle is for headless graphics
        : "";

    String javafxJarDownloadUrl = "https://download2.gluonhq.com/openjfx/${javafxVersionDownloadParentPath}/openjfx-${javafxVersion}_${javafxVersionSuffix}${osName}-${osArch.toString().replaceAll("^.*64", "x64")}_bin-sdk.zip";
    String javafxDownloadDir = "${gradle.rootProject.sourceSets.main.resources.srcDirs.getAt(0)}/javafx";
    String javafxDownloadZipFilePath = "${javafxDownloadDir}/openjfx.zip";

    def jfxDir = new File(javafxDownloadDir);

    if (!jfxDir.exists()) {
        jfxDir.mkdir();
    }

    if (
        jfxDir.listFiles().size() <= 0
            || (
            jfxDir.listFiles().size() == 1
                && ((String) jfxDir.list()[0]).matches(".*\\.(zip|tar\\.\\w+)\$")
        )
    ) {
        downloadFile(javafxDownloadZipFilePath, javafxJarDownloadUrl);

        /*
         * Copy individual .jar files out to resources dir for easy/direct usage.
         *
         *  - Only include .jar files.
         *  - Ignore empty directories.
         *  - Flatten file paths via `eachFile {}` closure.
         *
         * See:
         *  - https://stackoverflow.com/questions/40597202/how-to-copy-files-into-flat-directory-in-gradle/45635959#45635959
         */
        copy {
            from zipTree(javafxDownloadZipFilePath).matching {
                include "**/*.jar"
            }
            into javafxDownloadDir
            includeEmptyDirs = false
            eachFile {
                path = name
            }
        }

        delete javafxDownloadZipFilePath;
    }
}


// If the OS doesn't already have JavaFX installed, this will download the OS bindings + .jar files locally.
// Not needed since we use the `javafx` Gradle plugin.
//tasks.compileJava.doFirst { downloadJavaFX(); };


ext {
    findJavaFxJar = this.&findJavaFxJar;
    findAntJavaFxJar = this.&findAntJavaFxJar;
    getJavaFxJarPath = this.&getJavaFxJarPath;
    downloadJavaFX = this.&downloadJavaFX;
}
