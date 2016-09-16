package de.mlessmann.hwserver.installer;

import java.util.Map;

/**
 * Created by magnus.lessmann on 16.09.2016.
 */
public interface IRelease {
    String getVersion();
    int compareTo(String version2);
    Map<String,String> getFiles();
}
