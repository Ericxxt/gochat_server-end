package com.ericxxt.netty;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.springframework.stereotype.Component;

public class WSServerInitializer extends ChannelInitializer<SocketChannel> {

    @Override
    protected void initChannel(SocketChannel channel) throws Exception {
        ChannelPipeline pipeline=channel.pipeline();

        //websocket基于http协议，所以需要http编解码器
        pipeline.addLast(new HttpServerCodec());

        //对写大数据流的支持
        pipeline.addLast(new ChunkedWriteHandler());

        // the maxcontenlength
        //对httpMESSAGE 进行聚合，聚合成HTTPrequest AND HTTPresponse
        //几乎在netty中的编程都会运用到此聚合器
        pipeline.addLast(new HttpObjectAggregator(1024*26));

        //===========以上是对于http协议的支持

        //===========以下是支持心跳===================
        // 如果超过一分钟没有向服务器发送心跳，那么关闭连接
        // 检测，更新此时event状态
        pipeline.addLast(new IdleStateHandler(40,50,60));
        // 自定义的读写空闲状态检测 如果是all idle 那么断开
        pipeline.addLast(new HeartBeatHandler());


        //websocket 服务器处理的协议，用于指定给客户端链接访问的路由： /ws
        //本handlder会帮你处理一些繁重的复杂事物
        //会帮你进行握手 handshaking(close,ping,pong)
        //对于一些的websocket来说，都是用frames进行传输的，不同的数据类型对应的frames也不同

        pipeline.addLast(new WebSocketServerProtocolHandler("/ws"));

        //自定义的handlder
        pipeline.addLast(new ChatHandler());
    }
}
