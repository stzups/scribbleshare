import ClientMessageType from "../ClientMessageType.js";
import ClientMessage from "../ClientMessage.js";

export default class ClientMessageCanvasMove extends ClientMessage {
    constructor(canvasMovesMap) {
        super(ClientMessageType.CANVAS_MOVE);
        this.canvasMovesMap = canvasMovesMap;
    }

    serialize(writer) {
        super.serialize(writer);
        writer.writeUint8(this.canvasMovesMap.size);
        this.canvasMovesMap.forEach((canvasMoves, id) => {
            writer.writeInt16(id);
            writer.writeUint8(canvasMoves.size);
            canvasMoves.forEach((canvasMove) => {
                canvasMove.serialize(writer);
            });
        });
    }
}