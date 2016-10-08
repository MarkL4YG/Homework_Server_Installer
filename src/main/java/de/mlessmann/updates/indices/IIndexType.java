package de.mlessmann.updates.indices;

import java.util.List;

/**
 * Created by Life4YourGames on 27.09.16.
 */
public interface IIndexType {

    String forType();

    String uid();

    /**
     * Either Array or Object
     * Simply return false if this object is not valid for this type
     */
    boolean acceptJSON(Object o);

    List<IRelease> getReleases();

}
