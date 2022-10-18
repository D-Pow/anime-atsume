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

def copyDbToBuildDir() {
    copy {
        from dbName
        into warOutputDir
    }

    println("Copied ${dbName} to ${warOutputDir}")
}


ext {
    findJavaFxJar = this.&findJavaFxJar;
    findAntJavaFxJar = this.&findAntJavaFxJar;
    getJavaFxJarPath = this.&getJavaFxJarPath;
    copyDbToBuildDir = this.&copyDbToBuildDir;
}
