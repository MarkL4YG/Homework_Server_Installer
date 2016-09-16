package de.mlessmann.hwserver.installer.github;

import de.mlessmann.common.Common;
import de.mlessmann.hwserver.installer.IRelease;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Life4YourGames on 15.09.16.
 */
public class GitHubRelease implements IRelease {

    private JSONObject json;

    public GitHubRelease(JSONObject json) {
        this.json = json;
    }

    public String getVersion() {
        return Common.stripVersion(json.getString("tag_name"));
    }

    public int compareTo(String version) {
        return Common.compareVersions(this.getVersion(), version);
    }

    public Map<String, String> getFiles() {
        return accumulateFiles();
    }

    public String getBranch() {
        return json.getString("target_commitish");
    }

    public int getID() {
        return json.getInt("id");
    }

    //----

    private Map<String,String> accumulateFiles() {
        Map<String,String> files = new HashMap<String,String>();
        JSONArray a = json.getJSONArray("assets");
        a.forEach(o -> {
            if (o instanceof JSONObject) {
                String name = ((JSONObject) o).getString("name");
                String url = ((JSONObject) o).getString("browser_download_url");
                files.put(name, url);
            }
        });
        return files;
    }

}
