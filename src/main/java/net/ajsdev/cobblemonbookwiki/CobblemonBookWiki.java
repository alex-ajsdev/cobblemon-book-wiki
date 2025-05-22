package net.ajsdev.cobblemonbookwiki;


import net.ajsdev.cobblemonbookwiki.command.WikiCommand;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CobblemonBookWiki implements ModInitializer {
    public static final String MOD_ID = "cobblemonbookwiki";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);


    @Override
    public void onInitialize() {
        LOGGER.info("Registering Commands");
        CommandRegistrationCallback.EVENT.register((
                dispatcher,
                registryAccess,
                environment) -> {
            WikiCommand.register(dispatcher);
        });
    }
}
