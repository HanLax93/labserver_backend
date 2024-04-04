package site.handglove.labserver.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

public class ShellCommandRunner {
    public static ArrayList<String> run(String command) {
        ArrayList<String> commandRes = new ArrayList<>();
        try {
            ProcessBuilder builder = new ProcessBuilder("bash", "-c", command);
            builder.redirectErrorStream(true);
            Process process = builder.start();

            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                commandRes.add(line);
            }
            process.waitFor();

            int exitcode = process.exitValue();
            if (exitcode != 0) {
                System.out.println("exitcode for < " + command + " >: " + exitcode);
                System.out.println("\n---------------------------\n");
                System.out.println(commandRes);
                System.out.println("\n---------------------------\n");
            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return commandRes;
    }
}