package netty;

import common.ExecutorHelper;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyDataCollector {

    private static final Logger logger = LoggerFactory.getLogger(NettyDataCollector.class);

    public static void main(String[] args) throws InterruptedException {
        final int port = 8081;
        final EventLoopGroup bossGroup = new NioEventLoopGroup(0,
                ExecutorHelper.threadNameThreadFactory("bossGroup"));
        final EventLoopGroup workerGroup = new NioEventLoopGroup(0,
                ExecutorHelper.threadNameThreadFactory("workerGroup"));
        try {
            final ServerBootstrap bootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ServerInitializer())
                    .option(ChannelOption.SO_BACKLOG, 1024);
            final ChannelFuture future = bootstrap.bind(port).sync();
            logger.info(String.format("netty data collector running on [%d]", port));
            future.channel().closeFuture().sync();
        } catch (final InterruptedException ex) {
            logger.error("netty data collector running error: [%s]", ex);
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }

}
