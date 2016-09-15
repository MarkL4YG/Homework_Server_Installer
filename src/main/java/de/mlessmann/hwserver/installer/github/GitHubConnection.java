package de.mlessmann.hwserver.installer.github;

import de.mlessmann.common.HTTP;
import de.mlessmann.logging.ILogReceiver;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;

/**
 * Created by Life4YourGames on 15.09.16.
 */
public class GitHubConnection {

    private String url;
    private List<GitHubRelease> releases;
    private ILogReceiver r;

    public GitHubConnection(ILogReceiver r) {
        this.r = r;
    }

    public void setURL(String url) {
        this.url = url;
    }

    public boolean getIndex() {
        try {
            return readFrom(HTTP.GET(url, null));
        } catch (IOException e) {
            r.onMessage(this, Level.SEVERE, "Unable to get index from GitHub: " + e.toString());
            r.onException(this, Level.SEVERE, e);
            return false;
        }
    }

    private boolean readFrom(String index) {
        try {
            final boolean[] success = new boolean[]{true};
            JSONArray a = new JSONArray(index);
            a.forEach(o -> {
                if (o instanceof JSONObject)
                    success[0] = success[0] && tryReadJSONToRelease(((JSONObject) o));
            });
            return success[0];
        } catch (JSONException e) {
            r.onMessage(this, Level.SEVERE, "Unable to read index: " + e.toString());
            r.onException(this, Level.SEVERE, e);
            return false;
        }
    }

    private boolean tryReadJSONToRelease(JSONObject j) {
        boolean valid =
                j.has("url")
                        && j.has("id")
                        && j.has("tag_name")
                        && j.has("target_commitish")
                        && j.has("draft")
                        && j.has("prerelease")
                        && j.has("assets")
                        //just a divider
                        && (j.get("url") instanceof String)
                        && (j.get("id")instanceof Integer)
                        && (j.get("tag_name") instanceof String)
                        && (j.get("target_commitish") instanceof String)
                        && (j.get("draft") instanceof Boolean)
                        && (j.get("prerelease") instanceof Boolean)
                        && (j.get("assets") instanceof JSONArray);
        if (!valid) {
            r.onMessage(this, Level.WARNING, "Skipping invalid release");
            return true;
        }
        releases.add(new GitHubRelease(j));
        return true;
    }

}
