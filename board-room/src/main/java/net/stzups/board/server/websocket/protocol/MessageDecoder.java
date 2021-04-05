package net.stzups.board.server.websocket.protocol;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import net.stzups.board.server.ServerInitializer;
import net.stzups.board.server.websocket.protocol.client.ClientMessage;
import net.stzups.board.server.websocket.protocol.client.ClientMessageType;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageCreateDocument;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageDeleteDocument;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageHandshake;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageOpenDocument;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageUpdateCanvas;
import net.stzups.board.server.websocket.protocol.client.messages.ClientMessageUpdateDocument;

import javax.naming.OperationNotSupportedException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.logging.Logger;

/**
 * Decodes a WebSocketFrame sent by the client to a ClientPacket
 */
@ChannelHandler.Sharable
public class MessageDecoder extends MessageToMessageDecoder<WebSocketFrame> {
    @Override
    protected void decode(ChannelHandlerContext ctx, WebSocketFrame webSocketFrame, List<Object> list) throws Exception {
        Logger logger = ctx.channel().attr(ServerInitializer.LOGGER).get();

        if (webSocketFrame instanceof TextWebSocketFrame) {
            logger.warning("Got TextWebSocketFrame, content:");
            logger.warning(((TextWebSocketFrame) webSocketFrame).text());
        } else if (webSocketFrame instanceof BinaryWebSocketFrame) {
            ByteBuf byteBuf = webSocketFrame.content();
            ClientMessageType clientMessageType = ClientMessageType.valueOf(byteBuf.readUnsignedByte());
            ClientMessage clientMessage = ClientMessage.getClientMessage(clientMessageType, byteBuf);
            logger.info("recv " + clientMessage.getClass().getSimpleName());
            list.add(clientMessage);
        }
    }
}
