package de.mlessmann.updates.indices;

import com.google.common.base.Predicate;
import de.mlessmann.common.annotations.IndexType;
import de.mlessmann.logging.ILogReceiver;
import de.mlessmann.updates.Updater;
import org.reflections.Reflections;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static java.util.logging.Level.*;

/**
 * Created by Life4YourGames on 27.09.16.
 */
public class IndexTypeProvider implements IIndexTypeProvider {

    private ILogReceiver log = ILogReceiver.Dummy.newDummy();
    private List<IIndexType> types = new ArrayList<IIndexType>();
    private Updater updater;
    public static URLClassLoader loader = (URLClassLoader) IndexTypeProvider.class.getClassLoader();

    public IndexTypeProvider() {
        super();
    }

    public IndexTypeProvider(ILogReceiver r) {
        this();
        this.setLogReceiver(r);
    }

    public void setClassLoader(URLClassLoader loader) { this.loader = loader; }

    public void setLogReceiver(ILogReceiver r) {
        log = r;
    }

    public void setUpdater(Updater updater) {
        this.updater = updater;
    }

    // --- --- --- Work --- --- ---

    public void findAndLoadAll() {
        types.clear();

        URL[] urls = loader.getURLs();

        //Set up filters
        //String partPkgName = "/^.*(<name>).*$/ig";
        String partPkgName = "^.*(indices).*$*";

        @SuppressWarnings("Guava") Predicate<String> filter = new FilterBuilder().include(partPkgName);

        //Set up configuration builder

        ConfigurationBuilder cBuilder = new ConfigurationBuilder();

        cBuilder.filterInputsBy(filter);
        cBuilder.setUrls(urls);

        Reflections ref = new Reflections(cBuilder);

        Set<Class<?>> classes = ref.getTypesAnnotatedWith(IndexType.class);
        log.onMessage(this, FINER, classes.size() + " candidates for IndexType.class");
        classes.stream().filter(c1 -> c1 != null)
                .forEach(this::loadFromClass);
    }

    private void loadFromClass(Class<?> c) {
        if (!IIndexType.class.isAssignableFrom(c)) {
            log.onMessage(this, INFO, "Class " + c.toString() + " does not implement IIndexType, but is annotated!");
            return;
        }

        try {
            log.onMessage(this, FINEST, "Instantiating new IndexType from " + c.toString());
            Object o = null;
            try {
                o = c.getDeclaredConstructor(Updater.class).newInstance(updater);
            } catch (NoSuchMethodException ex1) {
                try {
                    o = c.getDeclaredConstructor().newInstance();
                } catch (NoSuchMethodException ex2) {
                    //Do not care
                }
            }
            if (o == null) o = c.newInstance();

            IIndexType h = (IIndexType) o;
            registerType(h);
        } catch (Exception e) {
            log.onMessage(this, WARNING, "Unable to create IIndexType from class \"" + c.toString() + "\": " + e.toString());
            e.printStackTrace();
        }
    }

    private void registerType(IIndexType i) {
        boolean add[] = {true};
        types.forEach(t -> {
            add[0] = add[0] && !t.uid().equals(i.uid());
        });
        if (add[0]) {
            log.onMessage(this, FINE, "Registering \"" + i.uid() + "\" for \"" + i.forType() + "\"");
            types.add(i);
        } else {
            log.onMessage(this, FINER, "Skipping \"" + i.uid() + "\" already registered");
        }
    }

    @Override
    public List<IIndexType> getTypes() {
        return types;
    }

    @Override
    public List<IIndexType> getTypesFor(String iType) {
        List<IIndexType> l = new ArrayList<IIndexType>();
        types.forEach(t -> {
            if (t.forType().equals(iType))
                l.add(t);
        });
        return l;
    }
}
