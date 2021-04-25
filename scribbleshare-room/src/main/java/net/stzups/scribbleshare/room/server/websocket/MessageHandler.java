package net.stzups.scribbleshare.room.server.websocket;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import net.stzups.scribbleshare.data.objects.Document;
import net.stzups.scribbleshare.data.objects.InviteCode;
import net.stzups.scribbleshare.data.objects.User;
import net.stzups.scribbleshare.data.objects.canvas.canvasUpdate.CanvasUpdate;
import net.stzups.scribbleshare.room.ScribbleshareRoom;
import net.stzups.scribbleshare.room.server.HttpAuthenticator;
import net.stzups.scribbleshare.room.server.ServerInitializer;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.ClientMessage;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.messages.ClientMessageCanvasUpdate;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.messages.ClientMessageDeleteDocument;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.messages.ClientMessageHandshake;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.messages.ClientMessageMouseMove;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.messages.ClientMessageOpenDocument;
import net.stzups.scribbleshare.room.server.websocket.protocol.client.messages.ClientMessageUpdateDocument;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageAddUser;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageCanvasUpdate;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageDeleteDocument;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageGetInvite;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageHandshake;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageMouseMove;
import net.stzups.scribbleshare.room.server.websocket.protocol.server.messages.ServerMessageUpdateDocument;

public class MessageHandler extends SimpleChannelInboundHandler<ClientMessage> {
    private enum State {
        HANDSHAKE,
        READY,
    }

    private State state = null;

    private Client client;
    private Room room;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        if (room != null) {
            room.removeClient(client);
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object event) {
        if (event instanceof WebSocketServerProtocolHandler.HandshakeComplete) {
            ServerInitializer.getLogger(ctx).info("WebSocket connection initialized");
            state = State.HANDSHAKE;
        }
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ClientMessage message) {
        switch (state) {
            case HANDSHAKE: {
                switch (message.getMessageType()) {
                    case HANDSHAKE: {
                        ClientMessageHandshake clientPacketHandshake = (ClientMessageHandshake) message;
                        User user = ScribbleshareRoom.getDatabase().getUser(ctx.channel().attr(HttpAuthenticator.USER).get());
                        if (user == null) {
                            ServerInitializer.getLogger(ctx).warning("User with does not exist");//todo
                            return;
                        }
                        ServerInitializer.getLogger(ctx).info("Handshake with invite " + clientPacketHandshake.getCode() + ", " + user);

                        state = State.READY;
                        client = new Client(user, ctx.channel());
                        client.queueMessage(new ServerMessageHandshake(client));
                        InviteCode inviteCode = ScribbleshareRoom.getDatabase().getInviteCode(clientPacketHandshake.getCode());
                        client.queueMessage(new ServerMessageAddUser(client.getUser()));
                        //figure out which document to open first
                        if (inviteCode != null) {
                            Document document = ScribbleshareRoom.getDatabase().getDocument(inviteCode.getDocument());
                            if (document != null) {
                                //if this isn't the user's own document and this isn't part of the user's shared documents then add and update
                                if (document.getOwner() != client.getUser().getId()) {
                                    if (!client.getUser().getSharedDocuments().add(document.getId())) {
                                        ScribbleshareRoom.getDatabase().updateUser(client.getUser());
                                    }
                                }
                                room = Room.getRoom(document);
                            } else {
                                ServerInitializer.getLogger(ctx).warning(client + " somehow used invite code for non existent document");
                                return;
                                //NPE will be thrown later
                            }
                        } else {
                            if (client.getUser().getOwnedDocuments().size() == 0) {
                                ScribbleshareRoom.getDatabase().createDocument(client.getUser());
                            }
                        }
                        client.getUser().getOwnedDocuments().removeIf((id) -> {
                            Document document = ScribbleshareRoom.getDatabase().getDocument(id);
                            if (document == null) {
                                return true;
                            } else {
                                client.queueMessage(new ServerMessageUpdateDocument(ScribbleshareRoom.getDatabase().getDocument(id)));
                                return false;
                            }
                        });//todo this is bad
                        client.getUser().getSharedDocuments().removeIf((id) -> {
                            Document document = ScribbleshareRoom.getDatabase().getDocument(id);
                            if (document == null) {
                                return true;
                            } else {
                                client.queueMessage(new ServerMessageUpdateDocument(ScribbleshareRoom.getDatabase().getDocument(id)));
                                return false;
                            }
                        });
                        client.flushMessages();
                        break;
                    }
                    default:
                        throw new UnsupportedOperationException("Unsupported message type " + message.getMessageType() + " for state " + state);
                }
                break;
            }
            case READY: {
                switch (message.getMessageType()) {
                    case MOUSE_MOVE: {
                        room.queueMessageExcept(new ServerMessageMouseMove(client, ((ClientMessageMouseMove) message).getMouseMoves()), client);
                        break;
                    }
                    case CANVAS_UPDATE: {
                        CanvasUpdate[] canvasUpdates = ((ClientMessageCanvasUpdate) message).getCanvasUpdates();
                        room.getCanvas().update(canvasUpdates);
                        room.queueMessageExcept(new ServerMessageCanvasUpdate(canvasUpdates), client);
                        break;
                    }
                    case OPEN_DOCUMENT: {
                        ClientMessageOpenDocument clientPacketOpenDocument = (ClientMessageOpenDocument) message;
                        Document document = ScribbleshareRoom.getDatabase().getDocument(clientPacketOpenDocument.getId());
                        if (document != null) {
                            if (room != null) {
                                room.removeClient(client);
                            }
                            room = Room.getRoom(document);
                            room.addClient(client);
                        } else {
                            ServerInitializer.getLogger(ctx).warning(client + " tried to open document not that does not exist");
                        }
                        break;
                    }
                    case CREATE_DOCUMENT: {
                        if (room != null) {
                            room.removeClient(client);
                        }
                        try {
                            room = Room.getRoom(ScribbleshareRoom.getDatabase().createDocument(client.getUser()));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        client.sendMessage(new ServerMessageUpdateDocument(room.getDocument()));
                        room.addClient(client);
                        break;
                    }
                    case DELETE_DOCUMENT: {
                        ClientMessageDeleteDocument clientMessageDeleteDocument = (ClientMessageDeleteDocument) message;
                        if (clientMessageDeleteDocument.id() == room.getDocument().getId()) {
                            System.out.println("document is delete ROOM");
                            room.sendMessage(new ServerMessageDeleteDocument(room.getDocument()));
                            room.end();
                            ScribbleshareRoom.getDatabase().deleteDocument(room.getDocument());
                            break;
                        }
                        Document document = ScribbleshareRoom.getDatabase().getDocument(clientMessageDeleteDocument.id());
                        if (document == null) {
                            ServerInitializer.getLogger(ctx).warning(client + " tried to delete document that does not exist");
                            break;
                        }
                        if (document.getOwner() != client.getUser().getId()) {
                            ServerInitializer.getLogger(ctx).warning(client + " tried to delete document they do not own");
                            break;
                        }
                        System.out.println("document is delete");
                        ScribbleshareRoom.getDatabase().deleteDocument(room.getDocument());
                        //Room.getRoom(document);
                        break;//todo better update logic
                    }
                    case UPDATE_DOCUMENT: {
                        ClientMessageUpdateDocument clientMessageUpdateDocument = (ClientMessageUpdateDocument) message;
                        if (clientMessageUpdateDocument.getName().length() > 64) {
                            ServerInitializer.getLogger(ctx).warning(client + " tried to change name to string that is too long (" + clientMessageUpdateDocument.getName().length() + ")");
                            break;
                        }
                        room.getDocument().setName(clientMessageUpdateDocument.getName());
                        ScribbleshareRoom.getDatabase().updateDocument(room.getDocument());
                        break;//todo better update logic
                    }
                    case GET_INVITE: {
                        client.sendMessage(new ServerMessageGetInvite(ScribbleshareRoom.getDatabase().getInviteCode(room.getDocument())));
                        break;
                    }
                    default:
                        throw new UnsupportedOperationException("Unsupported message type " + message.getMessageType() + " for state " + state + " sent by " + client);
                }
                break;
            }
        }
    }
}
