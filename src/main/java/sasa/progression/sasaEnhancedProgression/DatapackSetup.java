package sasa.progression.sasaEnhancedProgression;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.jspecify.annotations.NullMarked;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Objects;

@NullMarked
public class DatapackSetup implements PluginBootstrap {

    public static final String DATAPACK_NAMESPACE = "techtree";

    @Override
    public void bootstrap(BootstrapContext context) {
        context.getLifecycleManager().registerEventHandler(LifecycleEvents.DATAPACK_DISCOVERY.newHandler(
                event -> {
                    try {
                        URI uri = Objects.requireNonNull(getClass().getResource("/techtreedatapack")).toURI();
                        event.registrar().discoverPack(uri, DATAPACK_NAMESPACE);
                    } catch (URISyntaxException | IOException e) {
                        throw new RuntimeException(e);
                    }
                }
        ));
    }
}
