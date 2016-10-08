package de.mlessmann.upgrades;

import de.mlessmann.common.Common;
import de.mlessmann.common.zip.Unzip;
import de.mlessmann.logging.ILogReceiver;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by Life4YourGames on 28.09.16.
 */
public class Installer {

    private ILogReceiver log = ILogReceiver.Dummy.newDummy();

    private List<IInstallAction> installActions = null;

    public Installer() {
        super();
        installActions = new ArrayList<IInstallAction>();
    }

    public Installer(ILogReceiver log) {
        this();
        setLogReceiver(log);
    }

    // --- --- Getter --- --- --- ---

    public ILogReceiver getLogReceiver() {
        return log;
    }

    // --- --- Setter --- --- --- ---

    public void setLogReceiver(ILogReceiver log) {
        this.log = log;
    }

    // --- --- Run    --- --- --- ---

    public boolean setup() {
        installActions.clear();

        StringBuilder b = new StringBuilder();
        JSONObject c = null;
        try (BufferedReader r = new BufferedReader(new FileReader(new File("install.json")))) {
            r.lines().forEach(l -> b.append(l).append("\n"));
            c = new JSONObject(b.toString());
        } catch (IOException | JSONException e) {
            log.onMessage(this, Level.SEVERE, "Unable to run install -> Setup task failed: " + e.toString());
            log.onException(this, Level.SEVERE, e);
            return false;
        }

        boolean valid = c.has("actions")
                && c.optJSONArray("actions") != null;
        if (!valid) {
            log.onMessage(this, Level.SEVERE, "Unable to run install -> Setup task failed: Invalid configuration detected!");
        }

        JSONArray actions = c.getJSONArray("actions");

        boolean[] aborted = {false};
        actions.forEach(o -> {
            if (aborted[0])
                return;
            if (o instanceof JSONObject) {
                JSONObject j = ((JSONObject) o);
                IInstallAction i = IInstallAction.of(j);
                if (i == null || !i.isValid()) {
                    log.onMessage(this, Level.WARNING, "Invalid InstallAction! " + (i != null ? i.getName() : "null"));
                    aborted[0] = true;
                    return;
                }
                installActions.add(i);
            }
        });
        if (aborted[0]) {
            log.onMessage(this, Level.SEVERE, "Unable to run install -> Setup task failed: Invalid configuration detected!");
            return false;
        }

        log.onMessage(this, Level.INFO, "Setup done -> " + installActions.size() + " tasks registered.");
        return true;
    }

    public boolean run() {
        log.onMessage(this, Level.INFO, "Running install tasks");
        try {
            installActions.stream()
                    .filter(a -> a.getRuntime() == ActionRuntime.RUN)
                    .forEach(this::runTask);
        } catch (RuntimeException e) {
            log.onMessage(this, Level.SEVERE, "Install failed: a task failed to complete: " + e.toString());
            log.onException(this, Level.SEVERE, e);
            return false;
        }
        return true;
    }

    private boolean runTask(IInstallAction a) {
        log.onMessage(this, Level.FINER, "Running task: " + a.getName());
        ActionType t = a.getType();
        switch (t) {
            case COPY:
                runCopyTask(a);
                break;
            case EXTRACT:
                runDeflateTask(a);
                break;
            case CREATE:
                runMKTask(a);
                break;
            case RUN:
                runStartTask(a);
                break;
            default:
                log.onMessage(this, Level.WARNING, "Unknown task type: " + t.toString());
                return false;
        }
        return true;
    }

    private boolean runCopyTask(IInstallAction a) {
        String from = a.<String>getField("from");
        String to = a.<String>getField("to");
        if (from == null || to == null) {
            String msg = "Unable to run copyTask: " + (from != null ? from : "null") + "->" + (to != null ? to : "null");
            log.onMessage(this, Level.SEVERE, msg);
            throw new RuntimeException(msg);
        }
        File fFrom = new File(from);
        File fTo = new File(to);
        try {
            Common.copyFile(fFrom, fTo);
        } catch (IOException e) {
            String msg = "Unable to run copyTask: " + from + "->" + to + ": " + e.toString();
            log.onMessage(this, Level.SEVERE, msg);
            throw new RuntimeException(msg, e);
        }
        return true;
    }

    private boolean runDeflateTask(IInstallAction a) {
        String from = a.<String>getField("from");
        String to = a.<String>getField("to");
        if (from == null || to == null) {
            String msg = "Unable to run deflateTask: " + (from != null ? from : "null") + "->" + (to != null ? to : "null");
            log.onMessage(this, Level.SEVERE, msg);
            throw new RuntimeException(msg);
        }
        Unzip unzip = new Unzip(from, to);
        try {
            if (!unzip.unzip()) {
                String msg = "Unable to run deflateTask: " + from + "->" + to;
                log.onMessage(this, Level.SEVERE, msg);
                throw new RuntimeException(msg);
            }
            ;
        } catch (IOException e) {
            String msg = "Unable to run copyTask: " + from + "->" + to + ": " + e.toString();
            log.onMessage(this, Level.SEVERE, msg);
            throw new RuntimeException(msg, e);
        }
        return true;
    }

    private boolean runMKTask(IInstallAction a) {
        String from = a.<String>getField("from");
        String to = a.<String>getField("to");
        if (from == null || to == null) {
            String msg = "Unable to run MKTask: " + (from != null ? from : "null") + "->" + (to != null ? to : "null");
            log.onMessage(this, Level.SEVERE, msg);
            throw new RuntimeException(msg);
        }
        try (FileWriter w = new FileWriter(to)) {
            w.write(from);
            w.flush();
        } catch (IOException e) {
            String msg = "Unable to run MKTask: " + from + "->" + to + ": " + e.toString();
            log.onMessage(this, Level.SEVERE, msg);
            throw new RuntimeException(msg, e);
        }
        return true;
    }

    private boolean runStartTask(IInstallAction a) {
        JSONArray command = a.<JSONArray>getField("command");
        if (command == null) {
            String msg = "Unable to run startTask: no command supplied";
            log.onMessage(this, Level.SEVERE, msg);
            throw new RuntimeException(msg);
        }
        Boolean inheritIO = a.<Boolean>optField("inheritIO", Boolean.TRUE);

        List<String> c = new ArrayList<String>();
        command.forEach(o -> {
            if (o instanceof String) {
                String s = (String) o;
                //Apply some aliases
                s = s.replace("%java%", System.getProperty("java.home"));
                c.add(s);
            }
        });

        ProcessBuilder b = new ProcessBuilder();
        b.command(c);
        if (inheritIO)
            b.inheritIO();
        try {
            b.start();
        } catch (IOException e) {
            String msg = "Unable to run startTask: " + e.toString();
            log.onMessage(this, Level.SEVERE, msg);
            throw new RuntimeException(msg, e);
        }
        return true;
    }

    public boolean finish() {
        log.onMessage(this, Level.INFO, "Running finalize tasks");
        try {
            installActions.stream()
                    .filter(a -> a.getRuntime() == ActionRuntime.FINALIZE)
                    .forEach(this::runTask);
        } catch (RuntimeException e) {
            log.onMessage(this, Level.SEVERE, "Finalize failed: a task failed to complete: " + e.toString());
            log.onException(this, Level.SEVERE, e);
            return false;
        }
        return true;
    }
}
