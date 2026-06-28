package org.mini_lab.personal_cloud_sync.support;

public class CommandUtils {

    public static boolean commandExists(String command) {
        try {
            Process process = new ProcessBuilder(command, "--version").start();
            return process.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }
}
