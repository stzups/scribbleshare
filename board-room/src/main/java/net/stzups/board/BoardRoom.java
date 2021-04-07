package net.stzups.board;

import io.netty.channel.ChannelFuture;
import net.stzups.board.data.database.Database;
import net.stzups.board.data.database.memory.MemoryDatabase;
import net.stzups.board.data.database.postgres.PostgresDatabase;
import net.stzups.board.server.Server;
import net.stzups.board.util.LogFactory;
import net.stzups.board.util.config.Config;
import net.stzups.board.util.config.ConfigBuilder;
import net.stzups.board.util.config.configs.ArgumentConfig;
import net.stzups.board.util.config.configs.EnvironmentVariableConfig;
import net.stzups.board.util.config.configs.PropertiesConfig;

import java.util.Random;
import java.util.logging.Logger;

public class BoardRoom {
    private static Logger logger;
    private static Config config;
    private static final Random random = new Random();

    private static Database database;//user id -> user

    public static void main(String[] args) throws Exception {
        logger = LogFactory.getLogger("Board Room");

        logger.info("Starting Board Room server...");

        long start = System.currentTimeMillis();

        config = new ConfigBuilder()
                .addConfig(new ArgumentConfig(args))
                .addConfig(new PropertiesConfig("board.properties"))
                .addConfig(new EnvironmentVariableConfig("board."))
                .build();

        if (BoardRoom.getConfig().getBoolean(BoardConfigKeys.POSTGRES)) {
            logger.info("Connecting to Postgres database...");
            database = new PostgresDatabase(BoardRoom.getConfig().getString(BoardConfigKeys.POSTGRES_URL),
                    BoardRoom.getConfig().getString(BoardConfigKeys.POSTGRES_USER),
                    BoardRoom.getConfig().getString(BoardConfigKeys.POSTGRES_PASSWORD),
                    BoardRoom.getConfig().getInteger(BoardConfigKeys.POSTGRES_RETRIES));
            logger.info("Connected to Postgres database");
        } else {
            logger.warning("Using debug only runtime database. No data will be persisted.");
            database = new MemoryDatabase();
        }

        Server server = new Server();
        ChannelFuture channelFuture = server.start();

        logger.info("Started Board Room server in " + (System.currentTimeMillis() - start) + "ms");

        channelFuture.sync();

        start = System.currentTimeMillis();

        logger.info("Stopping Board Room server");

        server.stop();

        logger.info("Stopped Board Room server in " + (System.currentTimeMillis() - start) + "ms");
    }

    public static Logger getLogger() {
        return logger;
    }

    public static Config getConfig() {
        return config;
    }

    public static Database getDatabase() {
        return database;
    }

    public static Random getRandom() {
        return random;
    }
}
