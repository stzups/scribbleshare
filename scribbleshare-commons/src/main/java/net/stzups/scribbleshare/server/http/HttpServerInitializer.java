package net.stzups.scribbleshare.server.http;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.traffic.GlobalTrafficShapingHandler;
import io.netty.handler.traffic.TrafficCounter;
import net.stzups.scribbleshare.Scribbleshare;

import javax.net.ssl.SSLException;
import java.io.File;

@ChannelHandler.Sharable
public class HttpServerInitializer extends ChannelInitializer<SocketChannel> {
    public interface Config {
        boolean getSSL();
        String getSSLRootPath();
        String getSSLPath();
        boolean getDebugLogTraffic();
    }

    private final Config config;
    private final SslContext sslContext;

    private final DebugLog debugLog = new DebugLog();
    private final HttpHandler httpHandler = new HttpHandler();
    private final HttpExceptionHandler httpExceptionHandler = new HttpExceptionHandler();

    protected HttpServerInitializer(Config config) throws SSLException {
        this.config = config;

        if (config.getSSL()) {
            sslContext = SslContextBuilder.forServer(
                    new File(config.getSSLRootPath()),
                    new File(config.getSSLPath()))
                    .build();
        } else {
            sslContext = null;
        }
    }

    protected HttpExceptionHandler httpExceptionHandler() {
        return httpExceptionHandler;
    }

    @Override
    protected void initChannel(SocketChannel channel) {
        Scribbleshare.setLogger(channel);

        channel.pipeline().addLast(debugLog);
        if (config.getDebugLogTraffic()) channel.pipeline().addLast(new GlobalTrafficShapingHandler(channel.eventLoop(), 0, 0, 1000) {
            @Override
            protected void doAccounting(TrafficCounter counter) {
                System.out.print("\rread " + (double) counter.lastReadThroughput() / 1000 * 8 + "kb/s, write " + (double) counter.lastWriteThroughput() / 1000 * 8 + "kb/s");
            }
        });
        if (sslContext != null) channel.pipeline().addLast(sslContext.newHandler(channel.alloc()));
        channel.pipeline()
                .addLast(new HttpServerCodec())
                .addLast(new HttpObjectAggregator(Integer.MAX_VALUE)) //2gb todo decrease
                .addLast(httpHandler);
    }
}
