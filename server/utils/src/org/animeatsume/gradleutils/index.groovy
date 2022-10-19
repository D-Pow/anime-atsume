/**
 * Getting the current source file (as opposed to the default behavior of getting the file called in the CLI):
 *     - Answer: https://stackoverflow.com/questions/24528242/how-can-i-find-the-path-of-the-current-gradle-script/36527087#36527087
 *     - Similar: https://stackoverflow.com/questions/19451480/get-path-of-current-file
 *         + Similar: https://stackoverflow.com/questions/17027540/this-getclass-getresource-getpath-returns-an-incorrect-path
 *     - Resources only: https://stackoverflow.com/questions/49980430/gradle-how-to-retrieve-default-resource-directory-to-add-a-file
 *     - Wants to be correct (official docs): https://docs.gradle.org/current/javadoc/org/gradle/api/initialization/dsl/ScriptHandler.html#getSourceFile--
 *     - Close but no cigar: https://stackoverflow.com/questions/54695079/how-to-get-alljava-from-sourcesets-with-kotlin-gradle-dsl-in-gradle-4-10-3
 *
 * Relevant:
 *     - Properties application: https://stackoverflow.com/questions/62871387/gradle-how-to-use-the-external-final-variable-in-gradle-files
 */
String currentFile = buildscript.sourceFile;
String currentDir = new File("${currentFile}").getParent().toString().replaceAll("(?<=/|^)\\.(?=/|\$)", "");

apply from: "${currentDir}/Files.groovy"
apply from: "${currentDir}/Cli.groovy"
apply from: "${currentDir}/Objects.groovy"
apply from: "${currentDir}/JavaFX.groovy"
apply from: "${currentDir}/Frontend.groovy"
