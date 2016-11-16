package de.mlessmann.updates;

import de.mlessmann.common.Common;
import de.mlessmann.common.HTTP;
import de.mlessmann.logging.ILogReceiver;
import de.mlessmann.updates.indices.IIndexType;
import de.mlessmann.updates.indices.IIndexTypeProvider;
import de.mlessmann.updates.indices.IndexTypeProvider;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

import static java.util.logging.Level.*;

/**
 * Created by Life4YourGames on 27.09.16.
 */
public class Updater {

    public static String DEFCONF = "file://updateConfig.json";
    public static String DEFCONFNAME = "updateConfig.json";

    public static String DEFINDEX = "updateIndex.json";

    private ILogReceiver log;

    //--- --- Misc. --- --- ---
    private String rev = null;

    //--- --- Config --- --- ---
    private String confURI = DEFCONF;
    private File configFile = null;

    //--- --- Index --- --- ---
    private String indexURI = null;
    private String indexType = null;
    private File indexFile = null;

    private IIndexTypeProvider provider;
    private List<IIndexType> succeededTypes = new ArrayList<IIndexType>();


    public Updater() {
        log = ILogReceiver.Dummy.newDummy();
    }

    // --- --- Getter --- --- --- ---

    public ILogReceiver getLogReceiver() { return log; };
    public String getConfURI() { return confURI; }

    // --- --- Setter --- --- --- ---

    public void setLogReceiver(ILogReceiver log) { this.log = log; }
    public void setConfURI(String confURI) {
        if (confURI.equals(this.confURI)) return;
        this.confURI = confURI;
        this.configFile = null;
        this.indexType = null;
        this.indexFile = null;
        this.succeededTypes.clear();
    }
    public void setRevision(String rev) { this.rev = rev; }

    // --- --- Run    --- --- --- ---

    public void initProvider() {
        provider = new IndexTypeProvider(log);
        ((IndexTypeProvider) provider).setUpdater(this);
        ((IndexTypeProvider) provider).findAndLoadAll();
    }

    // --- --- RETR CONF

    public boolean retrieveConfig() {
        configFile = null;

        String uriType = confURI.substring(0, confURI.indexOf("://"));
        String l = confURI.replace(uriType+"://", "");

        boolean s = false;
        switch (uriType) {
            case "file": s = retrConfigFILE(l); break;
            case "resource": s = retrConfigRES(l); break;
            case "http": s = retrConfigURL(confURI); break;
            case "https": s = retrConfigURL(confURI); break;
            case "url": s = retrConfigURL(confURI); break;
        }

        return s;
    }

    private boolean retrConfigFILE(String path) {
        File f = new File(path);
        if (!f.isFile()) {
            log.onMessage(this, SEVERE, "Unable to initialize config: Not such file \"" + f.getAbsolutePath() + "\"");
            return false;
        }
        configFile = f;
        return true;
    }

    private boolean retrConfigRES(String path) {
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream(path);
            File f = new File(DEFCONFNAME);
            FileOutputStream out = new FileOutputStream(f);
            log.onMessage(this, INFO, "Streaming resource to fs -> " + f.getAbsolutePath());
            Common.copyStream(in, out, null);
            return retrConfigFILE(DEFCONFNAME);
        } catch (IOException e) {
            log.onMessage(this, SEVERE, "Unable to get config from resource: " + e.toString());
            log.onException(this, SEVERE, e);
            return false;
        }
    }

    private boolean retrConfigURL(String url) {
        try {
            File f = new File(DEFCONFNAME);
            log.onMessage(this, INFO, "Downloading to fs -> " + f.getAbsolutePath());
            if (!HTTP.GETFILE(url, f))  {
                log.onMessage(this, SEVERE, "Unknown error while trying to receive file from: " + url);
                return false;
            }
            return retrConfigFILE(DEFCONFNAME);
        } catch (IOException e) {
            log.onMessage(this, SEVERE, "Unable to get config from resource: " + e.toString());
            log.onException(this, SEVERE, e);
            return false;
        }
    }

    // --- --- READ CONF

    public boolean readConfig() {
        indexType = null;
        indexURI = null;
        indexFile = null;
        if (configFile == null || !configFile.isFile() || !configFile.canRead()) {
            log.onMessage(this, SEVERE, "Cannot read config: " + (configFile != null ? configFile.getAbsolutePath() : "null"));
            return false;
        }
        StringBuilder b = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(configFile))) {
            reader.lines().forEach(l -> b.append(l).append("\n"));

            JSONObject jFull = new JSONObject(b.toString());

            JSONObject jIndex = null;
            if (rev != null && !rev.isEmpty()) {
                jIndex = jFull.optJSONObject(jFull.optString(rev));
                if (jIndex == null) {
                    log.onMessage(this, SEVERE, "Invalid config: Revision \""+rev+"\" not found!");
                    return false;
                }
            } else {
                jIndex = jFull;
            }
            indexType = jIndex.optString("indexType");
            indexURI = jIndex.optString("indexURI");
            if ((indexType == null || indexType.isEmpty()) || (indexURI == null || indexURI.isEmpty())) {
                log.onMessage(this, SEVERE, "Invalid config! Cannot find indexType or -URI");
                indexType = null;
                indexURI = null;
                return false;
            }
            return true;
        } catch (IOException e) {
            log.onMessage(this, SEVERE, "Unable to read config: " + e.toString());
            log.onException(this, SEVERE, e);
            return false;
        }
    }

    // --- --- RETR INDEX

    public boolean retrieveIndex() {
        indexFile = null;

        String uriType = indexURI.substring(0, indexURI.indexOf("://"));
        String l = indexURI.replace(uriType+"://", "");

        boolean s = false;
        switch (uriType) {
            case "file": s = retrIndexFILE(l); break;
            case "resource": s = retrIndexRES(l); break;
            case "http": s = retrIndexURL(indexURI); break;
            case "https": s = retrIndexURL(indexURI); break;
            case "url": s = retrIndexURL(indexURI); break;
        }

        return s;
    }

    private boolean retrIndexFILE(String path) {
        File f = new File(path);
        if (!f.isFile()) {
            log.onMessage(this, SEVERE, "Unable to initialize index: Not such file \"" + f.getAbsolutePath() + "\"");
            return false;
        }
        indexFile = f;
        return true;
    }

    private boolean retrIndexRES(String path) {
        try {
            InputStream in = getClass().getClassLoader().getResourceAsStream(path);
            File f = new File(DEFINDEX);
            FileOutputStream out = new FileOutputStream(f);
            log.onMessage(this, INFO, "Streaming resource to fs -> " + f.getAbsolutePath());
            Common.copyStream(in, out, null);
            return retrIndexFILE(DEFINDEX);
        } catch (IOException e) {
            log.onMessage(this, SEVERE, "Unable to get index from resource: " + e.toString());
            log.onException(this, SEVERE, e);
            return false;
        }
    }

    private boolean retrIndexURL(String url) {
        try {
            File f = new File(DEFINDEX);
            log.onMessage(this, INFO, "Downloading to fs -> " + f.getAbsolutePath());
            if (!HTTP.GETFILE(url, f))  {
                log.onMessage(this, SEVERE, "Unknown error while trying to receive file from: " + url);
                return false;
            }
            return retrIndexFILE(DEFINDEX);
        } catch (IOException e) {
            log.onMessage(this, SEVERE, "Unable to get index from resource: " + e.toString());
            log.onException(this, SEVERE, e);
            return false;
        }
    }

    // --- --- READ INDEX

    public boolean readIndex() {
        succeededTypes.clear();
        if (indexFile == null || !indexFile.isFile() || !indexFile.canRead()) {
            log.onMessage(this, SEVERE, "Cannot read index: " + (indexFile != null ? indexFile.getAbsolutePath() : "null"));
            return false;
        }
        StringBuilder b = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(indexFile))) {
            reader.lines().forEach(l -> b.append(l).append("\n"));

            String iType = null;
            if (indexType.contains("->")) {
                iType = indexType.substring(indexType.indexOf("->") + 2);
            } else {
                iType = indexType;
            }

            Object[] o = {null};
            if (indexType.startsWith("{}")) {
                o[0] = new JSONObject(b.toString());
            } else if (indexType.startsWith("[]")) {
                o[0] = new JSONArray(b.toString());
            } else {
                log.onMessage(this, SEVERE, "Cannot process index: Unknown cast type!");
                return false;
            }
            List<IIndexType> types = provider.getTypesFor(iType);
            if (types.isEmpty()) {
                log.onMessage(this, SEVERE, "Unable to process index: No compatible type found");
                return false;
            }
            types.forEach(t -> {
                if (t.acceptJSON(o[0])) {
                    succeededTypes.add(t);
                }
            });
            if (succeededTypes.size() == 0) {
                log.onMessage(this, WARNING, "No type reported success!");
            }
            return true;
        } catch (IOException e) {
            log.onMessage(this, SEVERE, "Unable to read index: " + e.toString());
            log.onException(this, SEVERE, e);
            return false;
        }
    }

    // --- --- Results

    public List<IIndexType> getSucceededTypes() {
        return succeededTypes;
    }

}
