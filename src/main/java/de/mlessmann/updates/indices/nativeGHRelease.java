package de.mlessmann.updates.indices;

import de.mlessmann.common.Common;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Life4YourGames on 28.09.16.
 */
public class nativeGHRelease implements IRelease {

    private JSONObject json = null;

    public nativeGHRelease(JSONObject j) {
        this.json = j;
    }

    @Override
    public String version() {
        return Common.stripVersion(json.optString("tag_name"));
    }

    @Override
    public String branch() {
        return json.optString("target_commitish");
    }

    @Override
    public String name() {
        return json.optString("name", "null");
    }

    @Override
    public boolean isDraft() {
        return json.optBoolean("draft", true);
    }

    @Override
    public boolean isPreRelease() {
        return json.optBoolean("prerelease", true);
    }

    @Override
    public int compareTo(String v) {
        return Common.compareVersions(version(), v);
    }

    @Override
    public Map<String, String> files() {
        return mapAssets();
    }

    private Map<String, String> mapAssets() {
        JSONArray a = json.optJSONArray("assets");
        Map<String, String> m = new HashMap<String, String>();
        if (a!=null) {
            for (Object o : a) {
                if (!(o instanceof JSONObject))
                    continue;
                Object k = null;
                Object v = null;
                k = ((JSONObject) o).optString("name");
                v = ((JSONObject) o).optString("browser_download_url");
                if (k!=null && v!=null)
                    m.put(((String) k), ((String) v));
            }
        }
        return m;
    }
}
