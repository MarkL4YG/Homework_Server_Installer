package main;

import de.mlessmann.common.apparguments.AppArgument;
import de.mlessmann.install.Installer;

import java.util.List;
import java.util.logging.Logger;

/**
 * Created by Life4YourGames on 15.09.16.
 */
public class InstallerMain {

    public static void main(String[] args) {
        List<AppArgument> appArgs = AppArgument.fromArray(args);

        Installer i = new Installer(appArgs, Logger.getGlobal());
        i.run();
    }


}
