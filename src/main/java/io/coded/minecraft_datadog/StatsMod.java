package io.coded.minecraft_datadog;

import com.timgroup.statsd.NonBlockingStatsDClientBuilder;
import com.timgroup.statsd.StatsDClient;
import com.timgroup.statsd.StatsDClientErrorHandler;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.world.dimension.DimensionType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StatsMod implements ModInitializer, StatsDClientErrorHandler {
    public static final Logger LOGGER = LogManager.getLogger("DatadogStatsMod");

    private static StatsDClient client;

    public static void reportTick(double currentTickLength) {
        if (StatsMod.client == null) {
            LOGGER.warn("Datadog Client is null");
            return;
        }

        StatsMod.client.histogram("tick", currentTickLength);
    }

    public static void reportPlayers(int currentPlayerCount, int maxPlayerCount) {
        if (StatsMod.client == null) {
            LOGGER.warn("Datadog Client is null");
            return;
        }

        LOGGER.debug("logging player event to Datadog");

        StatsMod.client.gauge("players", currentPlayerCount);
        StatsMod.client.gauge("maxPlayers", maxPlayerCount);
    }

    public static void reportChunks(String dimension, int loadedChunkCount) {
        StatsMod.client.gauge("chunks", loadedChunkCount, "mc_dimension:" + dimension);
        //LOGGER.info("Dimension {} has {} loaded chunks", dimension, loadedChunkCount);
    }

    public static void reportEntities(String dimension, int entityCount, int blockEntityCount, int tickingBlockEntityCount) {
        StatsMod.client.gauge("entities", entityCount, "mc_dimension:"+dimension);
        StatsMod.client.gauge("blockEntities", blockEntityCount, "mc_dimension:"+dimension);
        StatsMod.client.gauge("tickingBlockEntities", tickingBlockEntityCount, "mc_dimension:"+dimension);
        //LOGGER.info("Dimension {} has {} entities, {} block entities, and {} ticking block entities", dimension, entityCount, blockEntityCount, tickingBlockEntityCount);
    }

    @Override
    public void onInitialize() {
        LOGGER.info("Starting DogStatsD Client");

        // We use a NonBlocking client so it doesn't throw errors in the game
        // loop and doesn't block execution, but we can still submit stats. This
        // should also make it easier to queue up stats, which will most likely happen
        // if we submit every tick length.
        client = new NonBlockingStatsDClientBuilder()
                .prefix("minecraft")
                .hostname("/tmp/dogstatsd.sock")
                .port(0)
                .errorHandler(this)
                .build();

        ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
            client.stop();
            client = null;
        });
    }

    @Override
    public void handle(Exception exception) {
        LOGGER.error("Datadog Error: " + exception);
    }
}
