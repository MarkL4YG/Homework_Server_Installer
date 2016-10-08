package de.mlessmann.upgrades;

import org.json.JSONObject;

/**
 * Created by Life4YourGames on 29.09.16.
 */
public interface IInstallAction {

    String getName();

    boolean isValid();

    ActionType getType();

    ActionRuntime getRuntime();

    <T> T getField(String key);

    <T> T optField(String key, T def);

    // --- Constructor

    public static IInstallAction of(JSONObject json) {
        return InstallAction.of(json);
    }
}
