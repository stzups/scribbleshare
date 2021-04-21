export default class CanvasDelete {
    constructor(reader) {
        this.dt = reader.readUint8();
        this.id = reader.readInt16();
    }

    serialize(writer) {
        writer.writeUint8(this.dt);
        writer.writeInt16(this.id);
    }

    static create(dt, id) {
        let object = Object.create(this.prototype);
        object.dt = dt;
        object.id = id;
        return object;
    }
}