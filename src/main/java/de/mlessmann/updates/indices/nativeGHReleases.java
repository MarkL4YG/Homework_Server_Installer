package de.mlessmann.updates.indices;

import de.mlessmann.common.annotations.IndexType;
import de.mlessmann.logging.ILogReceiver;
import de.mlessmann.updates.Updater;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static java.util.logging.Level.FINE;

/**
 * Created by Life4YourGames on 28.09.16.
 */
@IndexType
public class nativeGHReleases implements IIndexType {

    public static final String ID = "de.mlessmann.indices.github-releases";
    public static final String TYPE = "github-releases";

    private Updater updater;
    private ILogReceiver log;

    private List<IRelease> releases;

    public nativeGHReleases(Updater updater) {
        this.updater = updater;
        log = updater.getLogReceiver();
        releases = new ArrayList<IRelease>();
    }

    @Override
    public boolean acceptJSON(Object o) {
        if (!(o instanceof JSONArray))
            return false;
        releases.clear();

        ((JSONArray) o).forEach(o1 -> {
            if (o1 instanceof JSONObject) {
                tryLoadRelease(((JSONObject) o1));
            } else {
                log.onMessage(this, FINE, "Ignoring non-jObj entry");
            }
        });
        return true;
    }

    private boolean tryLoadRelease(JSONObject j) {
        boolean valid =
                j.has("tag_name") && j.optString("tag_name") != null
                && j.has("target_commitish") && j.optString("target_commitish") != null
                && j.has("name") && j.optString("name") != null
                && j.has("draft")
                && j.has("prerelease")
                && j.has("assets") && j.optJSONArray("assets") != null;
        if (valid) {
            releases.add(new nativeGHRelease(j));
        }
        return valid;
    }

    @Override
    public String forType() {
        return TYPE;
    }

    @Override
    public String uid() {
        return ID;
    }

    @Override
    public List<IRelease> getReleases() {
        return releases;
    }
}
