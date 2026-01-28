package cn.pupperclient.skimca;

import cn.pupperclient.skimca.event.SkimcaEventManager;
import cn.pupperclient.skimca.example.ExampleSkimca;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.loader.api.FabricLoader;

public class SkimcaClient implements ModInitializer, ClientModInitializer {
    private static final SkimcaEventManager eventManager = SkimcaEventManager.getInstance();
    public static final String Version = "26.1.1";

    @Override
    public void onInitialize() {
        autoRegisterEventHandlers();
    }

    private void autoRegisterEventHandlers() {
        try {
            eventManager.register(ExampleSkimca.class);

            registerFromEntrypoints();

            SkimcaLogger.info("SkimcaClient",
                    "Auto-registered " + eventManager.getRegisteredClassCount() + " event handler classes");
        } catch (Exception e) {
            SkimcaLogger.error("SkimcaClient", "Failed to auto-register event handlers", e);
        }
    }

    /**
     * Registers event handlers from Fabric entrypoints. <br/>
     * Other mods can declare event handlers in their fabric.mod.json: <br/>
     * "entrypoints": {
     *   "skimca:event_handlers": [
     *     "com.example.mod.MyEventHandler"
     *   ]
     * }
     */
    private void registerFromEntrypoints() {
        try {
            FabricLoader.getInstance().getEntrypointContainers("skimca:event_handlers", Object.class)
                    .forEach(container -> {
                        try {
                            Object handler = container.getEntrypoint();
                            eventManager.register(handler);
                            SkimcaLogger.info("SkimcaClient",
                                    "Registered event handler from mod: " + container.getProvider().getMetadata().getId());
                        } catch (Exception e) {
                            SkimcaLogger.error("SkimcaClient",
                                    "Failed to register event handler from mod: " +
                                            container.getProvider().getMetadata().getId(), e);
                        }
                    });
        } catch (NoSuchMethodError e) {
            // Fallback for older Fabric Loader versions
            SkimcaLogger.warn("SkimcaClient", "Fabric Loader entrypoints not available, using reflection scanning");
            Skimca.autoRegisterEventHandlers();
        }
    }

    public static SkimcaEventManager getEventManager() {
        return eventManager;
    }

    /**
     * Runs the mod initializer on the client environment.
     */
    @Override
    public void onInitializeClient() {

    }
}
