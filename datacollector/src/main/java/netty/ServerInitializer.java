package netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;

public class ServerInitializer extends ChannelInitializer<SocketChannel> {
    private static final int MAX_CONTENT_LENGTH = 1024 * 1024;

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        socketChannel.pipeline().addLast("http-codec", new HttpServerCodec());
        socketChannel.pipeline().addLast(new HttpObjectAggregator(MAX_CONTENT_LENGTH));
        socketChannel.pipeline().addLast("handler", new AsyncServerHandler());

    }
}
