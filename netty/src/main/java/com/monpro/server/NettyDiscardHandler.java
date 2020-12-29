package com.monpro.server;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class NettyDiscardHandler extends ChannelInboundHandlerAdapter {
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf byteBuf = (ByteBuf)msg;
        try {
            while (byteBuf.isReadable()) {
                System.out.println((char) byteBuf.readByte());
            }
            System.out.println();
        } finally{
            ReferenceCountUtil.release(msg);
        }
    }
}
