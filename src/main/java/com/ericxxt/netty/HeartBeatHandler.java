package com.ericxxt.netty;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * 用于处理客户端与服务端的心跳，在客户端空闲（如飞行模式)时关闭channel，节省服务器资源
 */
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {
    /**
     * 用户事件触发的处理器
     * @param ctx
     * @param evt
     * @throws Exception
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        // 判断evt是否是空闲idle事件中的一种，用于触发用户事件，包括读空闲，写空闲，读写空闲
        if(evt instanceof IdleStateEvent){
            IdleStateEvent event= (IdleStateEvent) evt;
            if(event.state()== IdleState.ALL_IDLE){
                ctx.channel().close();
                System.out.println("channel has remved");
            }
//            else if(event.state()== IdleState.WRITER_IDLE){
//                //写空闲，不做处理
//                //System.out.println("进入写空闲");
//            }else if(event.state()==IdleState.READER_IDLE){
//                //读空闲，不做处理
//                //System.out.println("进入读空闲");
//            }
        }
    }
}
