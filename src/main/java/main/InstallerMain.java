package main;

import de.mlessmann.common.zip.Unzip;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Life4YourGames on 15.09.16.
 */
public class InstallerMain {

    public static void main(String[] args) {

        File zipFile = new File("cache/update/update.zip");
        if (!zipFile.isFile()) {
            Logger.getGlobal().info("Unable to unpack update: Not found!");
            System.exit(1);
        }
        try {
            Logger.getGlobal().info("Deflating cache/update/update.zip");
            Unzip unzip = new Unzip(zipFile.getAbsolutePath(), ".");
            if (!unzip.unzip()) {
                Logger.getGlobal().severe("Unpacking failed!");
                System.exit(1);
            }
        } catch (IOException e) {
            Logger.getGlobal().severe("Unable to unpack update: Exception");
            e.printStackTrace();
            System.exit(1);
        }
        Logger.getGlobal().info("Unpacked update... searching for auto start file");

        File autoStart = new File("update_after_install");
        if (!autoStart.isFile()) {
            Logger.getGlobal().info("No auto start file found: Done");
            System.exit(0);
        }
        List<String> c = new ArrayList<String>();
        try (BufferedReader buff = new BufferedReader(new FileReader(autoStart))) {
            buff.lines().forEach(c::add);
        } catch (IOException e) {
            Logger.getGlobal().severe("Unable to read auto start file: Exception");
            e.printStackTrace();
            System.exit(1);
        }
        ProcessBuilder pb = new ProcessBuilder();
        pb.inheritIO();
        pb.command(c);
        try {
            pb.start();
        } catch (Exception e) {
            Logger.getGlobal().severe("Unable to start auto start: Exception");
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
