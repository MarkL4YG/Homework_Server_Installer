package main;

import de.mlessmann.common.apparguments.AppArgument;
import de.mlessmann.logging.MarkL4YGLogger;
import de.mlessmann.upgrades.Installer;

import java.util.List;
import java.util.logging.Level;

/**
 * Created by Life4YourGames on 15.09.16.
 */
public class InstallerMain {

    public static void main(String[] args) {
        List<AppArgument> appArgs = AppArgument.fromArray(args);

        MarkL4YGLogger l = MarkL4YGLogger.get("main");
        l.setLevel(Level.FINEST);
        l.setLogTrace(false);

        Installer i = new Installer();
        i.setLogReceiver(l.getLogReceiver());

        if (!i.setup()) {
            l.getLogger().severe("Setup failed: Exiting");
            System.exit(1);
        }
        if (!i.run()) {
            l.getLogger().severe("Run failed: Exiting");
            System.exit(1);
        }
        if (!i.finish()) {
            l.getLogger().severe("Finalization failed: Exiting");
            System.exit(1);
        }
    }
}
