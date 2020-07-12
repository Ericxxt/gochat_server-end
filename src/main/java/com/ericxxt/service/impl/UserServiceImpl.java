package com.ericxxt.service.impl;


import com.ericxxt.enums.MsgActionEnum;
import com.ericxxt.enums.PreSearchFriendsEnum;
import com.ericxxt.mapper.*;
import com.ericxxt.netty.ChatData;
import com.ericxxt.netty.DataContent;
import com.ericxxt.netty.UserChannelRelation;
import com.ericxxt.pojo.ChatMsg;
import com.ericxxt.pojo.FriendsRequest;
import com.ericxxt.pojo.MyFriends;
import com.ericxxt.pojo.Users;
import com.ericxxt.pojo.vo.FriendRequestVo;
import com.ericxxt.pojo.vo.MyFriendsVo;
import com.ericxxt.push.AsyncCenter;
import com.ericxxt.service.UserService;
import com.ericxxt.utils.FastDFSClient;
import com.ericxxt.utils.FileUtils;
import com.ericxxt.utils.JsonUtils;
import com.ericxxt.utils.QRCodeUtils;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

//    @Autowired
//    private  UsersMapper usersMapper;
//
//    @Autowired
//    private  Sid sid;
    private final UsersMapper usersMapper;
    private final Sid sid;
    private final QRCodeUtils qrCodeUtils;
    private final FastDFSClient fastDFSClient;
    private final MyFriendsMapper myFriendsMapper;
    private final FriendsRequestMapper friendsRequestMapper;
    private final AsyncCenter asyncCenter;
    private final UsersMapperCustom usersMapperCustom;
    private final ChatMsgMapper chatMsgMapper;
    //here this method couldn't be autowired because no implementable usersMapper
    public UserServiceImpl(UsersMapper usersMapper,Sid sid,QRCodeUtils qrCodeUtils,FastDFSClient fastDFSClient,MyFriendsMapper myFriendsMapper,
                           FriendsRequestMapper friendsRequestMapper,AsyncCenter asyncCenter,UsersMapperCustom usersMapperCustom
    ,ChatMsgMapper chatMsgMapper){
        this.usersMapper=usersMapper;
        this.sid=sid;
        this.qrCodeUtils=qrCodeUtils;
        this.fastDFSClient=fastDFSClient;
        this.myFriendsMapper=myFriendsMapper;
        this.friendsRequestMapper=friendsRequestMapper;
        this.asyncCenter=asyncCenter;
        this.usersMapperCustom=usersMapperCustom;
        this.chatMsgMapper=chatMsgMapper;
    }
    //查询需要添加事务
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public boolean queryUsernameIsExist(String username) {
        Users user=new Users();
        user.setUsername(username);
        Users result=usersMapper.selectOne(user);
        return result!=null;
    }

    //查询需要添加事务
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserForLogin(String username, String pwd) {
        Example userExample=new Example(Users.class);

        //制定标准,教其如何一一对应
        Example.Criteria criteria=userExample.createCriteria();

        criteria.andEqualTo("username",username);
        criteria.andEqualTo("password",pwd);

        Users result=usersMapper.selectOneByExample(userExample);
        return result;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users saveUser(Users user) {

        //generate a unique id for each user
        String userId=sid.nextShort();
        user.setId(userId);
        //set the default image for users
        user.setFaceImage("M00/00/00/ChtaJl78kb6Af5TkAAY40hAcHrU974_80x80.png");
        user.setFaceImageBig("M00/00/00/ChtaJl78kb6Af5TkAAY40hAcHrU974.png");
        //generate a unique QRcode for each user
        String qrfile_path="/Users/Erictang/QRfile/"+user.getUsername()+user.getId()+"qrcode.png";
        String qrcodeContent="GoChat_QR:"+user.getUsername();
        qrCodeUtils.createQRCode(qrfile_path,qrcodeContent);
        // now the qr code is a file, we convert it into bytes can store in db
        MultipartFile file= FileUtils.fileToMultipart(qrfile_path);
        String qr_url="";
        try {
            qr_url = fastDFSClient.uploadQRCode(file);
        }catch (IOException e){
            e.printStackTrace();
        }
        user.setQrcode(qr_url);
        int result=usersMapper.insert(user);
        return result==1?user:null;
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users updataUsersInfo(Users users) {
        usersMapper.updateByPrimaryKeySelective(users);
        //查询用户信息并返回
        return queryUsersInfo(users.getId());
    }


    // search for new friends
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Integer preSearchFriends(String myUserId, String username) {
        Users user=queryUserByUsername(username);
        //the user does not exist
        if(user==null){
            return PreSearchFriendsEnum.USER_NOT_FOUND.getStatus();
        }
        // you are searching for yourself
        if(myUserId.equals(user.getId())) return PreSearchFriendsEnum.USER_CAN_NOT_BE_YOURSELF.getStatus();
        String friendId=user.getId();
        // set the query standard, in this way, you don't have to write more mapper implements
        Example example=new Example(MyFriends.class);
        Example.Criteria criteria=example.createCriteria();
        criteria.andEqualTo("myUserId",myUserId);
        criteria.andEqualTo("myFriendUserId",user.getId());
        MyFriends myFriend= myFriendsMapper.selectOneByExample(example);
        if(myFriend == null){
            // he is not ur friend yet, you can add him
            return PreSearchFriendsEnum.SUCCESS.getStatus();
        }else{
            // you have been friends together already
            return PreSearchFriendsEnum.USER_ALREADY_BE_FRIEND.getStatus();
        }

    }

    //查询最新的用户信息
    private Users queryUsersInfo(String userId){
        return usersMapper.selectByPrimaryKey(userId);
    }
//    @Override
//    public Users updataUsersInfo(Users users) {
//        return null;
//    }
//
//
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public Users queryUserByUsername(String username) {
        Example example=new Example(Users.class);
        Example.Criteria criteria=example.createCriteria();
        criteria.andEqualTo("username",username);
        return usersMapper.selectOneByExample(example);
    }

    //发送添加好友请求
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void sendAddFriendRequest(String myUserId, String username) {
        // get the if from the new friend
        Users user=queryUserByUsername(username);
        String friendId=user.getId();
        // check if there is any same record  in db
        //因为你可以连续申请好友好几次，所以为了避免数据库中的重复所以需要判断之前是否有无申请过
        Boolean request = queryFriendRequest(myUserId,friendId);
        if(request){
            String requestId=sid.nextShort();
            FriendsRequest friendsRequest=new FriendsRequest();
            friendsRequest.setId(requestId);
            friendsRequest.setSendUserId(myUserId);
            friendsRequest.setAcceptUserId(friendId);
            friendsRequest.setRequestDateTime(new Date());
            friendsRequestMapper.insert(friendsRequest);
        }
        //  异步发送推送消息
        asyncCenter.sendPush("好友请求","您收到新的好友请求",user.getCid());

    }

    // 查询好友请求
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<FriendRequestVo> queryFriendRequestList(String acceptId) {
        return usersMapperCustom.queryFriendRequestList(acceptId);
    }

    // 处理 好友忽略
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void deleteFriendRequest(String sendUserId, String acceptUserId) {
        Example example=new Example(FriendsRequest.class);
        Example.Criteria criteria=example.createCriteria();
        criteria.andEqualTo("sendUserId",sendUserId);
        criteria.andEqualTo("acceptUserId",acceptUserId);
        friendsRequestMapper.deleteByExample(example);
    }

    //通过好友认证，实现好友添加
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void passFriendRequest(String sendUserId, String acceptUserId) {
        // bi-directional mutual 互相添加，所以操作两次
        saveFriends(sendUserId, acceptUserId);
        saveFriends(acceptUserId,sendUserId);
        deleteFriendRequest(sendUserId, acceptUserId);

//       使用websocket推送消息 更新发送方的通讯录
        Channel channel= UserChannelRelation.get(sendUserId);
        // 判断channel是否在线
        if(channel!=null){
            //使用websocket主动推送消息到请求发送者，更新他的通讯录
            DataContent dataContent=new DataContent();
            dataContent.setAction(MsgActionEnum.PULL_FRIEND.type);
            channel.writeAndFlush(new TextWebSocketFrame(JsonUtils.objectToJson(dataContent)));
        }

    }

    // 保存消息到数据库
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<MyFriendsVo> queryMyFriends(String userId) {
        return usersMapperCustom.queryMyFriends(userId);
    }

    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public String saveMsg(ChatData chatData) {
        ChatMsg chatMsg=new ChatMsg();
        String msgId=sid.nextShort();
        chatMsg.setId(msgId);
        chatMsg.setMsg(chatData.getMsg());
        chatMsg.setSendUserId(chatData.getSenderId());
        chatMsg.setAcceptUserId(chatData.getReceiverId());
        chatMsg.setCreateTime(new Date());
        chatMsg.setSignFlag(false);
        chatMsgMapper.insert(chatMsg);
        return msgId;
    }

    //批量签收消息
    @Transactional(propagation = Propagation.REQUIRED)
    @Override
    public void updateSignMsg(List<String> msgIdList) {
        usersMapperCustom.updateSignMsg(msgIdList);
    }

    //查询未读消息 , 指的是用户终端未收到的消息，并不是未读的消息
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public List<ChatMsg> queryUnReadMsg(String acceptUserId) {
        Example example=new Example(ChatMsg.class);
        Example.Criteria criteria=example.createCriteria();
        criteria.andEqualTo("acceptUserId",acceptUserId);
        criteria.andEqualTo("signFlag",false);
        List<ChatMsg> list=chatMsgMapper.selectByExample(example);
        return list;
    }

    //检查添加好友请求数据库中是否有相同记录
    private Boolean queryFriendRequest(String userId, String friendId){
        Example example = new Example(FriendsRequest.class);
        Example.Criteria criteria = example.createCriteria();
        criteria.andEqualTo("sendUserId",userId);
        criteria.andEqualTo("acceptUserId",friendId);
        FriendsRequest friendsRequest = friendsRequestMapper.selectOneByExample(example);
        //查不到，表示可以添加，返回true。否则返回false；
        return friendsRequest == null;
    }

    private void saveFriends(String senderId,String acceptUserId){
        MyFriends myFriends=new MyFriends();
        myFriends.setId(sid.nextShort());
        myFriends.setMyFriendUserId(senderId);
        myFriends.setMyUserId(acceptUserId);
        myFriendsMapper.insert(myFriends);
    }
}
