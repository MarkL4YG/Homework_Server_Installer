package de.mlessmann.updates.indices;

import de.mlessmann.common.annotations.Nullable;

import java.util.Map;

/**
 * Created by Life4YourGames on 27.09.16.
 */
public interface IRelease {

    @Nullable
    String branch();

    boolean isPreRelease();

    boolean isDraft();

    String name();

    String version();

    /**
     * @see de.mlessmann.common.Common#compareVersions(String, String)  for details
     */
    int compareTo(String v);

    /**
     * \<Name, URL\>
     */
    Map<String, String> files();


}
