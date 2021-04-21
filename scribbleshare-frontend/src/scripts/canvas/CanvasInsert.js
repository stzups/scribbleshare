import {getCanvasObject} from "./Canvas.js";
import CanvasObjectWrapper from "./CanvasObjectWrapper.js";

export default class CanvasInsert {
    constructor(canvasObjectType, reader) {
        this.dt = reader.readUint8();
        this.id = reader.readInt16();
        this.canvasObject = new CanvasObjectWrapper(canvasObjectType, getCanvasObject(canvasObjectType, reader));
    }

    serialize(writer) {
        writer.writeUint8(this.dt);
        writer.writeInt16(this.id);
        this.canvasObject.serialize(writer);
    }

    static create(dt, id, canvasObjectWrapper) {
        let object = Object.create(this.prototype);
        object.dt = dt;
        object.id = id;
        object.canvasObject = canvasObjectWrapper;
        return object;
    }
}