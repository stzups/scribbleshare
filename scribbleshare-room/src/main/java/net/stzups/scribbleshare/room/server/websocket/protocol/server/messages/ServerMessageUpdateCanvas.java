package net.stzups.scribbleshare.room.server.websocket.protocol.server.messages;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.object.CanvasObjectType;
import net.stzups.scribbleshare.data.objects.canvas.object.CanvasObjectWrapper;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.ServerMessageType;

import java.util.Map;

public class ServerMessageUpdateCanvas extends ServerMessage {
    private final  Map<CanvasObjectType, Map<Short, CanvasObjectWrapper[]>> canvasObjects;

    public ServerMessageUpdateCanvas(Map<CanvasObjectType, Map<Short, CanvasObjectWrapper[]>> canvasObjects) {
        super(ServerMessageType.UPDATE_CANVAS);
        this.canvasObjects = canvasObjects;
    }

    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeByte((byte) canvasObjects.size());
        for (Map.Entry<CanvasObjectType, Map<Short, CanvasObjectWrapper[]>> entry : canvasObjects.entrySet()) {
            byteBuf.writeByte((byte) entry.getKey().getId());
            byteBuf.writeShort((short) entry.getValue().size());
            for (Map.Entry<Short, CanvasObjectWrapper[]> entry1 : entry.getValue().entrySet()) {
                byteBuf.writeShort(entry1.getKey());
                byteBuf.writeByte((byte) entry1.getValue().length);
                for (CanvasObjectWrapper canvasObjectWrapper : entry1.getValue()) {
                    canvasObjectWrapper.serialize(byteBuf);
                }
            }
        }
    }
}
