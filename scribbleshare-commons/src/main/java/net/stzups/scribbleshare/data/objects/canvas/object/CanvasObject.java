package net.stzups.scribbleshare.data.objects.canvas.object;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.canvas.object.objects.CanvasImage;
import net.stzups.scribbleshare.data.objects.canvas.object.objects.Shape;

public class CanvasObject {
    private final short x;
    private final short y;
    private final short width;
    private final short height;
    private final short rotation;

    public CanvasObject(ByteBuf byteBuf) {
        x = byteBuf.readShort();
        y = byteBuf.readShort();
        width = byteBuf.readShort();
        height = byteBuf.readShort();
        rotation = byteBuf.readByte();
    }

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeShort(x);
        byteBuf.writeShort(y);
        byteBuf.writeShort(width);
        byteBuf.writeShort(height);
        byteBuf.writeByte(rotation);
    }

    public static CanvasObject getCanvasObject(CanvasObjectType canvasObjectType, ByteBuf byteBuf) {
        CanvasObject canvasObject;
        switch (canvasObjectType) {
            case SHAPE:
                canvasObject = new Shape(byteBuf);
                break;
            case IMAGE:
                canvasObject = new CanvasImage(byteBuf);
                break;
            default:
                canvasObject = null;
        }
        return canvasObject;
    }
}
