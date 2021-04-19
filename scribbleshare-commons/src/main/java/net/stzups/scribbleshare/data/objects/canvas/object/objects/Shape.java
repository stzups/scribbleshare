package net.stzups.scribbleshare.data.objects.canvas.object.objects;

import io.netty.buffer.ByteBuf;
import io.netty.util.collection.IntObjectHashMap;
import net.stzups.scribbleshare.data.objects.canvas.object.CanvasObject;
import net.stzups.scribbleshare.data.objects.canvas.object.CanvasObjectType;

import java.util.EnumSet;
import java.util.Map;

public class Shape extends CanvasObject {
    private enum Type {
        RECTANGLE(0),
        CIRCLE(1),
        ;

        private static final Map<Integer, Type> objectTypeMap = new IntObjectHashMap<>();
        static {
            for (Type canvasObjectType : EnumSet.allOf(Type.class)) {
                objectTypeMap.put(canvasObjectType.id, canvasObjectType);
            }
        }

        public final int id;

        Type(int id) {
            this.id = id;
        }

        public static Type valueOf(int id) {
            Type objectType = objectTypeMap.get(id);
            if (objectType == null) {
                throw new IllegalArgumentException("Unknown CanvasObjectType for given id " + id);
            }
            return objectType;
        }
    }

    private final Type type;

    public Shape(ByteBuf byteBuf) {
        super(byteBuf);
        this.type = Type.valueOf(byteBuf.readUnsignedByte());
    }

    public void serialize(ByteBuf byteBuf) {
        super.serialize(byteBuf);
        byteBuf.writeByte((byte) type.id);
    }
}
