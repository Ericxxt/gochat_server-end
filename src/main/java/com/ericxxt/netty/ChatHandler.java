package com.ericxxt.netty;

import com.ericxxt.enums.MsgActionEnum;
import com.ericxxt.mapper.UsersMapper;
import com.ericxxt.pojo.Users;
import com.ericxxt.push.AppPush;
import com.ericxxt.service.UserService;
import com.ericxxt.utils.JsonUtils;
import com.ericxxt.utils.SpringUtil;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.catalina.User;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//TextWebSocketFrame是netty中专门用于处理文本的对象，fram是消息的载体
public class ChatHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    //用于记录和管理所有客户端的channel       //定义channel集合,管理channel,传入全局事件执行器
    private static ChannelGroup users=new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

    /**
     * 定义信道的消息处理机制,该方法处理一次,故需要同时对所有客户端进行操作(channelGroup)
     * @param ctx 上下文
     * @param msg 文本消息
     * @throws Exception
     */
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, TextWebSocketFrame msg) throws Exception {
        //get the infor from the clients
        //step1: 获取客户端传过来的消息，对象类型为TextWebSocketFrame
        String text=msg.text();
        System.out.println("the msg of text is:"+text);
        //对传递过来的消息类型进行判断
        DataContent dataContent= JsonUtils.jsonToPojo(text,DataContent.class);
        // get the type action in the text
        Integer action =dataContent.getAction();
        //acquire the channel
        Channel currentChannel=ctx.channel();

        //step 2: 判断动作，如果是第一次open的时候，初始化channel，将其与userid一一对应
        if (action == MsgActionEnum.CONNECT.type) {
            // 将channel与userid放入对应关系中
            UserChannelRelation.put(dataContent.getChatData().getSenderId(),currentChannel);

            for(Channel c:users){
                System.out.println(c.id().asLongText());
            }
        }else if(action==MsgActionEnum.CHAT.type){
            //2.2 类型为聊天，此时需要把聊天记录保存到数据库，同时添加为未读状态
            ChatData chatData=dataContent.getChatData();
            String msgText=chatData.getMsg();
            String senderId=chatData.getSenderId();
            String receiverId=chatData.getReceiverId();
            //2.3 保存到数据库，获取service对象
            UserService userService= (UserService) SpringUtil.getBean("userServiceImpl");
            UsersMapper usersMapper= (UsersMapper) SpringUtil.getBean("usersMapper");
            String msgId=userService.saveMsg(chatData);
            // 得到msg的sid 唯一id，
            chatData.setMsgId(msgId);
            // 返回 一个new datacontent  我个人感觉和上面的datacontent没有区别，但是没有了action别的没用的变量
            DataContent returnContent=new DataContent();
            returnContent.setChatData(chatData);

            // 获取发信人还有接受人的对象，因为这样才可以获取Cid 码进行推送
            Users sender=usersMapper.selectByPrimaryKey(senderId);
            Users receiver=usersMapper.selectByPrimaryKey(receiverId);

            //2.3 发送消息，根据channel进行消息推送 ，因为receiverId 是唯一的，接收方才能收到消息

            Channel  receiverChannel=UserChannelRelation.get(receiverId);

            if(receiverChannel==null){
                //TODO 用户不在线, 推送消息到用户APP, (JPush,个推,小米推送等)
                AppPush.sendPush(sender.getNickname(),msgText,receiver.getCid());
                System.out.println("receiver now is not online!");
            }else{
                // 去channel group 中查找对应的channel是否存在
                //
                Channel findChannel= users.find(receiverChannel.id());
                if(findChannel!=null){
                    //用户在线，发送chatdata回客户端接收
                    receiverChannel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(returnContent)));
                    System.out.println("send msg to receiver!");
                }else{
                    //用户离线
                    AppPush.sendPush(sender.getNickname(),msgText,receiver.getCid());
                }
            }
        }else if(action==MsgActionEnum.SIGNED.type){
            //2.3 签收类型的消息，对未签收的消息进行状态的改变，改成签收状态
            //!!!!!!注意这里的签收不是用户行为不是已读未读，而是对方的手机有没有接收到自己的消息
            //2.3.1 获取service
            UserService userService= (UserService) SpringUtil.getBean("userServiceImpl");
            //2.3.2 对于签收类型的消息,前台传递过来的msgId在extend字段中
            // 注意extent字段是前台传递过来的msgId
            String msgIdStr= dataContent.getExtend();
            //2.3.3 切分字符串获取msgId数组
            String[] msgIds=msgIdStr.split(",");
            //2.3.4 转换为list
            List<String> msgIdList=new ArrayList<>();
            for(String msgId:msgIds){
                if(StringUtils.isNoneBlank(msgId)){
                    msgIdList.add(msgId);
                }
            }
            //2.3.5 现在list中的都是需要更新签收状态的消息，调用数据库进行签收
            userService.updateSignMsg(msgIdList);
        }else if(action==MsgActionEnum.KEEPALIVE.type){
            System.out.println("收到"+currentChannel+"的心跳");
        }



    }

    /**
     * 当客户端连接服务端之后(打开连接)----->handlerAdded
     * 获取客户端的channel,并且放到ChannelGroup中去管理
     * @param ctx
     * @throws Exception
     */
    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        users.add(ctx.channel());
    }

    /**
     * 发生异常时，关闭连接（channel），随后将channel从ChannelGroup中移除
     * @param ctx
//     * @param cause
     * @throws Exception
     */
    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        // when the handlerRemoved is called , remove is automatically
        // 是自动的，还是选择写代码是为了规范
        //打印要被移除的channel
        String asShortText=ctx.channel().id().asShortText();
        System.out.println("the channel to be removed:"+asShortText);
        users.remove(ctx.channel());
//        System.out.println(ctx.channel().id().asLongText());
//        System.out.println(ctx.channel().id().asShortText());
    }
    /**
     * 发生异常时，关闭连接（channel），随后将channel从ChannelGroup中移除
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        System.out.println("出错啦, 原因是:"+cause.getMessage());
        ctx.channel().close();
        users.remove(ctx.channel());
    }
}
