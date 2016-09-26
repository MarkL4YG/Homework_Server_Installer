package de.mlessmann.install;

import de.mlessmann.common.apparguments.AppArgument;
import de.mlessmann.common.zip.Unzip;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Life4YourGames on 25.09.16.
 */
public class Installer implements Runnable {

    private int delay;
    private boolean update = false;
    private boolean interrupted = false;
    private String prefix = "Homework_Server_";
    private Logger logger;

    public Installer(List<AppArgument> args, Logger logger) {
        this.logger = logger;
        args.forEach(a -> {
            String k = a.getKey();
            String v = a.getValue();
            switch (k) {
                case "--delay":
                    delay = Integer.parseInt(v);
                    break;
                case "--update":
                    update = true;
                    break;
                case "--file":
                    prefix = v;
                    break;
                default:
                    logger.warning("Unknown argument: " + k);
                    break;
            }
        });
    }

    @Override
    public void run() {
        if (update) {
            update();
        } else {
            install();
        }
    }

    private void install() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(System.in))) {
            logger.info("This will install and set up the HW server, do you want to continue? [y/n]");
            while (true) {
                String ln = reader.readLine();
                if ("n".equalsIgnoreCase(ln)) {
                    logger.info("Okay, exiting...");
                    System.exit(0);
                }
                if ("y".equalsIgnoreCase(ln)) {
                    break;
                }
            }
            File dir = new File(".");
            String[] files = dir.list();
            List<String> zips = new ArrayList<String>();
            if (files != null) {
                Arrays.stream(files).forEach(s -> {
                    if (s.startsWith(prefix) && s.endsWith(".zip"))
                        zips.add(s);
                });
            } else {
                logger.severe("No installable zip found!");
                logger.severe("Make sure to download " + prefix + "*.zip too");
                System.exit(1);
            }

            int index = 0;
            if (zips.size() > 1) {
                try (BufferedReader rd = new BufferedReader(new InputStreamReader(System.in))) {
                    while (true) {
                        logger.info("Selected file: " + zips.get(index));
                        String ln = reader.readLine();
                        if ("y".equalsIgnoreCase(ln)) {
                            break;
                        }
                        if ("exit".equalsIgnoreCase(ln)) {
                            logger.info("Okay, exiting...");
                            System.exit(0);
                        }
                        if (++index == files.length)
                            index = 0;
                    }
                } catch (IOException e) {
                    logger.warning(e.toString());
                }
            }
            Unzip unzip = new Unzip(zips.get(index), ".");
            logger.info("Deflating " + zips.get(index));
            if (!unzip.unzip()) {
                logger.severe("Cannot deflate \"" + zips.get(index) + "\"");
                System.exit(1);
            }
            logger.info("Done! You may now start the server. For example by using:");
            logger.info("java -jar Homework_Server.jar");
            System.exit(0);
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }


    private void update() {
        if (delay>0) {
            logger.info("Delaying execution for " + delay + "ms");
            sleep(delay);
            if (interrupted) {
                logger.info("Delay interrupted");
                System.exit(1);
            }
        }
        File zipFile = new File("cache/update/update.zip");
        if (!zipFile.isFile()) {
            logger.info("Unable to unpack update: Not found!");
            System.exit(1);
        }
        try {
            logger.info("Deflating cache/update/update.zip");
            Unzip unzip = new Unzip(zipFile.getAbsolutePath(), ".");
            if (!unzip.unzip()) {
                logger.severe("Unpacking failed!");
                System.exit(1);
            }
        } catch (IOException e) {
            logger.severe("Unable to unpack update: Exception");
            e.printStackTrace();
            System.exit(1);
        }
        logger.info("Unpacked update... searching for auto start file");

        File autoStart = new File("update_after_install");
        if (!autoStart.isFile()) {
            logger.info("No auto start file found: Done");
            System.exit(0);
        }
        List<String> c = new ArrayList<String>();
        try (BufferedReader buff = new BufferedReader(new FileReader(autoStart))) {
            buff.lines().forEach(c::add);
        } catch (IOException e) {
            logger.severe("Unable to read auto start file: Exception");
            e.printStackTrace();
            System.exit(1);
        }
        ProcessBuilder pb = new ProcessBuilder();
        pb.inheritIO();
        pb.command(c);
        try {
            pb.start();
        } catch (Exception e) {
            logger.severe("Unable to start auto start: Exception");
            e.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }

    public void sleep(int d) {
        try {
            Thread.sleep(d);
        } catch (InterruptedException e) {
            interrupted = true;
        }
    }

}
