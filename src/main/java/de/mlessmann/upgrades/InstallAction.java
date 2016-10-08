package de.mlessmann.upgrades;

import org.json.JSONObject;

/**
 * Created by Life4YourGames on 28.09.16.
 */
public class InstallAction implements IInstallAction {

    public static InstallAction of(JSONObject o) {
        boolean valid = o.has("name")
                && o.optString("name") != null
                && o.has("type")
                && o.optString("type") != null
                && o.has("runtime")
                && o.optString("runtime") != null;
        if (!valid)
            return null;

        return new InstallAction(o);
    }

    private JSONObject json;
    private ActionType type;
    private ActionRuntime runtime;

    public InstallAction(JSONObject j) {
        String t = j.getString("type");
        switch (t) {
            case "COPY":
                type = ActionType.COPY;
                break;
            case "EXTRACT":
                type = ActionType.EXTRACT;
                break;
            case "CREATE":
                type = ActionType.CREATE;
                break;
            case "RUN":
                type = ActionType.RUN;
                break;
            default:
                type = ActionType.INVALID;
                break;
        }
        t = j.getString("runtime");
        switch (t) {
            case "RUN":
                runtime = ActionRuntime.RUN;
                break;
            case "FINALIZE":
                runtime = ActionRuntime.FINALIZE;
                break;
            default:
                runtime = ActionRuntime.INVALID;
                break;
        }
        json = j;
    }

    @Override
    public <T> T getField(String key) {
        Object o = json.opt(key);
        if (o == null) return null;
        try {
            return (T) o;
        } catch (ClassCastException e) {
            return null;
        }
    }

    @Override
    public <T> T optField(String key, T def) {
        Object o = json.opt(key);
        if (o == null) return def;
        try {
            return (T) o;
        } catch (ClassCastException e) {
            return def;
        }
    }

    @Override
    public ActionType getType() {
        return type;
    }

    @Override
    public String getName() {
        return json.optString("name", "null");
    }

    @Override
    public boolean isValid() {
        return (type!=ActionType.INVALID);
    }

    @Override
    public ActionRuntime getRuntime() {
        return runtime;
    }
}
