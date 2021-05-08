package net.stzups.scribbleshare.room;

import io.netty.channel.ChannelFuture;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.data.database.ScribbleshareDatabase;
import net.stzups.scribbleshare.data.database.implementations.PostgresDatabase;
import net.stzups.scribbleshare.room.server.RoomServerInitializer;

public class ScribbleshareRoom extends Scribbleshare implements AutoCloseable {
    private final PostgresDatabase database;
    private final ScribbleshareRoomConfig config;

    private ScribbleshareRoom(String[] args) throws Exception {
        this(args, new ScribbleshareRoomConfigImplementation());
    }

    private ScribbleshareRoom(String[] args, ScribbleshareRoomConfigImplementation config) throws Exception {
        super(args, config);
        this.database = new PostgresDatabase(getConfig());
        this.config = config;
    }

    public static void main(String[] args) throws Exception {
        Scribbleshare.getLogger().info("Starting scribbleshare-room server...");
        long start = System.currentTimeMillis();
        try (ScribbleshareRoom scribbleshareRoom = new ScribbleshareRoom(args)) {

            ChannelFuture closeFuture = scribbleshareRoom.start(new RoomServerInitializer(scribbleshareRoom.getConfig()));

            Scribbleshare.getLogger().info("Started scribbleshare-room server in " + (System.currentTimeMillis() - start) + "ms");

            closeFuture.sync();
        }
    }

    public ScribbleshareDatabase getDatabase() {
        return database;
    }

    @Override
    public ScribbleshareRoomConfig getConfig() {
        return config;
    }

    @Override
    public void close() throws Exception {
        super.close();
        database.close();
    }
}
