package net.stzups.scribbleshare.server;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import io.netty.util.AttributeKey;
import net.stzups.scribbleshare.Scribbleshare;
import net.stzups.scribbleshare.data.database.databases.SessionDatabase;
import net.stzups.scribbleshare.data.objects.session.HttpSession;

import java.util.List;

import static net.stzups.scribbleshare.server.HttpUtils.send;

@ChannelHandler.Sharable
public class HttpAuthenticator extends MessageToMessageDecoder<FullHttpRequest> {
    public static AttributeKey<Long> USER = AttributeKey.valueOf(HttpAuthenticator.class, "USER");

    private final SessionDatabase sessionDatabase;

    public HttpAuthenticator(SessionDatabase sessionDatabase) {
        this.sessionDatabase = sessionDatabase;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, FullHttpRequest request, List<Object> out) {
        if (ctx.channel().attr(USER).get() != null) {
            out.add(request.retain());
            return;
        }

        Scribbleshare.getLogger(ctx).info(request.method() + " " + request.uri());

        if (request.decoderResult().isFailure()) {
            send(ctx, request, HttpResponseStatus.BAD_REQUEST);
            Scribbleshare.getLogger(ctx).info("Bad request");
            return;
        }

        if (!request.method().equals(HttpMethod.GET)) {
            send(ctx, request, HttpResponseStatus.METHOD_NOT_ALLOWED);
            Scribbleshare.getLogger(ctx).info("Bad method");
            return;
        }

        if (request.uri().equals("/healthcheck")) {
            send(ctx, request, HttpResponseStatus.OK);
            Scribbleshare.getLogger(ctx).info("Good healthcheck response");
            return;
        }
//todo
/*        if (!request.uri().equals(scribbleshare.getConfig().getString(ScribbleshareRoomConfigKeys.WEBSOCKET_PATH))) {
            send(ctx, request, HttpResponseStatus.NOT_FOUND);
            Scribbleshare.getLogger(ctx).info("Bad uri");
            return;
        }*/

        HttpSession.ClientCookie cookie = HttpSession.ClientCookie.getClientCookie(request, HttpSession.COOKIE_NAME);
        if (cookie != null) {
            HttpSession httpSession = sessionDatabase.getHttpSession(cookie.getId());
            if (httpSession != null && httpSession.validate(cookie.getToken())) {
                Scribbleshare.getLogger(ctx).info("Authenticated with id " + httpSession.getUser());
                ctx.channel().attr(USER).set(httpSession.getUser());
                //now that we have an good authenticated HTTP request, set up WebSocket pipeline
                //ctx.pipeline().remove(this);
                //pass on this request
                out.add(request.retain());
            } else {
                Scribbleshare.getLogger(ctx).warning("Bad authentication");
                //bad authentication attempt
                //todo rate limit timeout server a proper response???
                send(ctx, request, HttpResponseStatus.UNAUTHORIZED);
            }
        } else {
            Scribbleshare.getLogger(ctx).info("No authentication");
            send(ctx, request, HttpResponseStatus.UNAUTHORIZED);
        }
    }
}
