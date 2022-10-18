import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import org.gradle.process.ExecSpec;


def cliCmd(String commandAndArgs) {
    return cliCmd(commandAndArgs, null);
}
def cliCmd(String commandAndArgs, String dir) {
    return cliCmd(commandAndArgs, dir, false);
}
/**
 * See:
 *  - Gradle `exec` task docs: https://docs.gradle.org/current/dsl/org.gradle.api.tasks.Exec.html
 *  - Running a CLI command with pipes, `&&`, `||`, etc.: https://discuss.gradle.org/t/how-to-achive-bash-like-functionality-e-g-ls-grep-mystuff-with-gradle-project-exec/5516/4
 *  - Support Unix and Windows: https://stackoverflow.com/questions/27406054/how-do-you-support-a-gradle-exec-task-for-both-mac-and-pc
 *  - Spread lists (or simply pass arrays) in spread-operator functions: https://stackoverflow.com/questions/9863742/how-to-pass-an-arraylist-to-a-varargs-method-parameter/9863752#9863752
 *  - Get STD(IN|OUT|ERR) from Gradle terminal executions:
 *      + https://stackoverflow.com/questions/38071178/executing-shell-script-and-using-its-output-as-input-to-next-gradle-task/38072449#38072449
 *      + https://stackoverflow.com/questions/32198697/how-to-run-shell-script-from-gradle-and-wait-for-it-to-finish
 *  - Pass initial CLI args to nested terminal command: https://www.baeldung.com/gradle-command-line-arguments
 *  - Run terminal commands from Gradle script: https://stackoverflow.com/questions/159148/groovy-executing-shell-commands
 *  - Running Bash commands in Gradle: https://stackoverflow.com/questions/52389713/execute-bash-command-in-a-gradle-function
 *
 * Alternatives:
 *  - {@code "my command".execute().toString()}
 *      + Likely will require {@code cmd.execute().consumeProcessOutput(stdOut, stdErr)}
 *  - {@code executeCommand(cmd)}
 *  - {@code (Below with or without `.waitFor()`)}
 *  - {@code new String(cmd.execute()[.getOutputStream()|.toByteArray()][.toByteArray()])}
 *  - They may or may not require:
 *      + {@code .text.trim()}
 *      + {@code .consumeProcessOutput(stdOut, stdErr)} // Assuming the args are instances of {@code StringBuilder}
 *      + {@code .waitForProcessOutput(stdOut, stdErr)}
 *      + {@code new OutputStreamWriter(subProcess.[functionFromAbove]()).toString()}
 *  - Something akin to:
 *  <pre>
 *      public class StreamGobbler implements Runnable {
 *          private final InputStream inputStream;
 *          private final Consumer<String> consumer;
 *
 *          public StreamGobbler(InputStream inputStream, Consumer<String> consumer) {
 *              this.inputStream = inputStream;
 *              this.consumer = consumer;
 *          }
 *
 *          @Override
 *          public void run() {
 *              new BufferedReader(new InputStreamReader(inputStream)).lines()
 *                  .forEach(consumer);
 *          }
 *      }
 *      process = Runtime.getRuntime().exec(String.format("bash -c \"cd '%s'; %s\"", directory, command));
 *      StreamGobbler streamGobbler = new StreamGobbler(process.getInputStream(), System.out::println);
 *      Future<?> future = Executors.newSingleThreadExecutor().submit(streamGobbler);
 *      int exitCode = process.waitFor();
 *      assert exitCode == 0;
 *      return future.get(); // wait for command to finish
 *  </pre>
 */
def cliCmd(String commandAndArgs, String dir, boolean returnAllStd) {
    String[] shellCommand = isWindows
        ? new String[]{ "cmd", "/c" }
        : new String[]{ "bash", "-c" };

    List<String> commandLineEntries = ((List<String>) Arrays.asList(shellCommand, commandAndArgs)).flatten();

    ByteArrayInputStream stdIn = new ByteArrayInputStream();
    ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
    ByteArrayOutputStream stdErr = new ByteArrayOutputStream();

    project.exec {
        if (dir != null) {
            workingDir = dir;
        }

        standardInput(stdIn);
        standardOutput(stdOut);
        errorOutput(stdErr);

        ExecSpec processExitInfo = commandLine(commandLineEntries.toArray());
    }

    String stdInStr = stdIn.toString().trim();
    String stdOutStr = stdOut.toString().trim();
    String stdErrStr = stdErr.toString().trim();

    if (returnAllStd) {
        // Extract entries via `def (ret1, ret2, ret3) = myFunc();`
        //   See: https://stackoverflow.com/questions/24617348/multiple-return-syntax/24617510#24617510
        // TODO - Get exit code from cmd and return it here
        return [ stdInStr, stdOutStr, stdErrStr ];
    }

    return stdOutStr;
}


ext {
    cliCmd = this.&cliCmd;
}
