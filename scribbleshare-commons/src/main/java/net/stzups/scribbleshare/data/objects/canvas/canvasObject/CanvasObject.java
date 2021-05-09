package net.stzups.scribbleshare.data.objects.canvas.canvasObject;

import io.netty.buffer.ByteBuf;
import net.stzups.scribbleshare.data.objects.exceptions.DeserializationException;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.canvasObjects.CanvasImage;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.canvasObjects.CanvasMouse;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.canvasObjects.Line;
import net.stzups.scribbleshare.data.objects.canvas.canvasObject.canvasObjects.Shape;

public class CanvasObject {
    private short x;
    private short y;

    public CanvasObject(ByteBuf byteBuf) {
        x = byteBuf.readShort();
        y = byteBuf.readShort();
    }

    public short getX() {
        return x;
    }

    public short getY() {
        return y;
    }

    public void update(CanvasObject canvasObject) {
        this.x = canvasObject.x;
        this.y = canvasObject.y;
    }

    public void serialize(ByteBuf byteBuf) {
        byteBuf.writeShort(x);
        byteBuf.writeShort(y);
    }

    public static CanvasObject getCanvasObject(CanvasObjectType canvasObjectType, ByteBuf byteBuf) throws DeserializationException {
        CanvasObject canvasObject;
        switch (canvasObjectType) {
            case SHAPE:
                canvasObject = new Shape(byteBuf);
                break;
            case IMAGE:
                canvasObject = new CanvasImage(byteBuf);
                break;
            case MOUSE:
                canvasObject = new CanvasMouse(byteBuf);
                break;
            case LINE:
                canvasObject = new Line(byteBuf);
                break;
            default:
                canvasObject = null;
        }
        return canvasObject;
    }
}
