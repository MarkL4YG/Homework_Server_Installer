package de.mlessmann.updates.indices;

import java.util.List;

/**
 * Created by Life4YourGames on 27.09.16.
 */
public interface IIndexTypeProvider {

    List<IIndexType> getTypes();

    List<IIndexType> getTypesFor(String iType);

}
