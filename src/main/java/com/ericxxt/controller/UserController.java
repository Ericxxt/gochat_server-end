package com.ericxxt.controller;

import com.ericxxt.enums.OperatorFriendRequestTypeEnum;
import com.ericxxt.enums.PreSearchFriendsEnum;
import com.ericxxt.pojo.ChatMsg;
import com.ericxxt.pojo.Users;
import com.ericxxt.pojo.bo.UsersBo;
import com.ericxxt.pojo.vo.FriendRequestVo;
import com.ericxxt.pojo.vo.MyFriendsVo;
import com.ericxxt.pojo.vo.UsersVo;
import com.ericxxt.service.UserService;
import com.ericxxt.utils.FastDFSClient;
import com.ericxxt.utils.FileUtils;
import com.ericxxt.utils.MD5Utils;
import com.ericxxt.utils.ResponseJSONResult;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.util.List;

@RestController
@RequestMapping("user")
public class UserController {

    private final UserService userService;

    private final FastDFSClient fastDFSClient;

    @Autowired
    public UserController(UserService userService,FastDFSClient fastDFSClient){
        this.userService=userService;
        this.fastDFSClient=fastDFSClient;
    }
    /**
     * 判断登录还是注册, User中包含用户名和密码
     * @param user
     * @return
     */
    @PostMapping("/registerLogin")
    public ResponseJSONResult registerOrLogin(@RequestBody Users user) throws Exception {

        // 判断用户名和密码不能为空
        if(StringUtils.isBlank(user.getUsername())|| StringUtils.isBlank(user.getPassword())){
            return ResponseJSONResult.errorMsg("No blank name or password allowed!");
        }

        boolean usernameIsExist=userService.queryUsernameIsExist(user.getUsername());

        // define a users to get
        Users userResult=null;
        if(usernameIsExist){
            // 存在用户名，则取得对象登陆
            userResult=userService.queryUserForLogin(user.getUsername(), MD5Utils.getMD5Str(user.getPassword()));
            //password is wrong
            if(userResult==null){
                return ResponseJSONResult.errorMsg("Wrong password!");
            }

        }else{
            // no available user in database, register for one
            user.setNickname(user.getUsername());
            user.setFaceImage("");
            user.setFaceImageBig("");
            user.setPassword(MD5Utils.getMD5Str(user.getPassword()));
            userResult=userService.saveUser(user);
        }
        UsersVo usersVo=new UsersVo();
        BeanUtils.copyProperties(userResult,usersVo);
        return ResponseJSONResult.ok(usersVo);
    }
    /**
     * 用户上传头像逻辑
     * 1. 用户上传头像为Base64格式的字符串
     * 2. 将字符串转换为文件对象
     * 3. 上传到fastDFS
     * 4. 更新数据库的信息
     * @param usersBo
     * @return
     */
    @PostMapping("/uploadFaceBase64")
    public ResponseJSONResult uploadFaceBase64(@RequestBody UsersBo usersBo) throws Exception{
        //acquire the base64 string from the front-end
        //转化为文件对象之后再上传
        System.out.println("in the function already!");
        String base64Data=usersBo.getFaceData();
//        System.out.println("data:"+base64Data);

        String userFacePath = "/Users/Erictang/base64Path/"+ usersBo.getUserId() + "userFace64.png";

//        System.out.println("userFacePath:"+userFacePath);
        FileUtils.base64ToFile(userFacePath,base64Data);

        //upload the file to fastdfs
        MultipartFile faceFile = FileUtils.fileToMultipart(userFacePath);

        //when you upload the file successfully, you will get a url from fastdfs
        String url=fastDFSClient.uploadBase64(faceFile);
        System.out.println("urlForBase64:"+url);


        //下面是为了小图
        //获取缩略图的url
        //需要将原来的url地址比如         ABC.PNG
        // 变成                          ABC_80X80.PNG
        String  thump="_80x80.";
        String[] arr=url.split("\\.");
        String thumpImgUrl=arr[0]+thump+arr[1];

        Users  user=new Users();
        user.setId(usersBo.getUserId());
        user.setFaceImage(thumpImgUrl);
        user.setFaceImageBig(url);

        Users userInfo= userService.updataUsersInfo(user);

        UsersVo usersVo=new UsersVo();
        BeanUtils.copyProperties(userInfo,usersVo);

        return ResponseJSONResult.ok(usersVo);
    }

    /**
     * 更新用户的昵称
     * @param usersBo
     * @return
     */
    @PostMapping("/setNickname")
    public ResponseJSONResult setNickname(@RequestBody UsersBo usersBo){
        Users users=new Users();
        users.setId(usersBo.getUserId());
        users.setNickname(usersBo.getNickname());
        Users usersInfo=userService.updataUsersInfo(users);
        return ResponseJSONResult.ok(usersInfo);
    }

    /**
     * 搜索用户,根据账号做匹配查询而不是模糊查询
     * @param myUserId 我的用户id
     * @param username 搜索的用户名
     * @return
     */
    @PostMapping("/searchFriends")
    public ResponseJSONResult searchFriends(String myUserId,String username){
        // shouldn't be null
        if(StringUtils.isBlank(myUserId)||StringUtils.isBlank(username)) return ResponseJSONResult.errorMsg("blank info");
        // 前置条件 - 1. 搜索的用户如果不存在，返回[无此用户]
        // 前置条件 - 2. 搜索账号是你自己，返回[不能添加自己]
        // 前置条件 - 3. 搜索的朋友已经是你的好友，返回[该用户已经是你的好友]
        int result=userService.preSearchFriends(myUserId,username);
        if(result== PreSearchFriendsEnum.SUCCESS.getStatus()){
            Users user=userService.queryUserByUsername(username);
            UsersVo usersVo=new UsersVo();
            BeanUtils.copyProperties(user,usersVo);
            return ResponseJSONResult.ok(usersVo);
        }else{
            // get message by the status that we have
            String errMsg=PreSearchFriendsEnum.getMsgByStatus(result);
            return ResponseJSONResult.errorMsg(errMsg);
        }
    }

    /**
     * 发送添加好友的请求
     * @param myUserId
     * @param username
     * @return
     */
    @PostMapping("/sendAddFriendRequest")
    public ResponseJSONResult sendAddFriendRequest(String myUserId,String username){
        // shouldn't be null
        if(StringUtils.isBlank(myUserId)||StringUtils.isBlank(username)) return ResponseJSONResult.errorMsg("blank info");
        // 前置条件 - 1. 搜索的用户如果不存在，返回[无此用户]
        // 前置条件 - 2. 搜索账号是你自己，返回[不能添加自己]
        // 前置条件 - 3. 搜索的朋友已经是你的好友，返回[该用户已经是你的好友]
        int result=userService.preSearchFriends(myUserId, username);
        if(result==PreSearchFriendsEnum.SUCCESS.getStatus()){
            // you can add this one
            userService.sendAddFriendRequest(myUserId, username);
            return ResponseJSONResult.ok();
        }else{
            String errMsg=PreSearchFriendsEnum.getMsgByStatus(result);
            return ResponseJSONResult.errorMsg(errMsg);
        }
    }
    /**
     * 根据接收者的id，查询所有的好友请求列表
     * @param acceptUserId
     * @return
     */
    @PostMapping("/queryFriendRequests")
    public ResponseJSONResult queryFriendRequests(String acceptUserId){
        if(StringUtils.isBlank(acceptUserId)){
            return ResponseJSONResult.errorMsg("blank error");
        }
        List<FriendRequestVo> requestList=userService.queryFriendRequestList(acceptUserId);
//        System.out.println(requestList.get(0));
        if(requestList!=null){
            return ResponseJSONResult.ok(requestList);
        }else{
            return ResponseJSONResult.errorMsg("No new requests so far!");
        }
    }
    /**
     * 操作好友请求
     * @param acceptUserId 接收者id
     * @param senderId 发送请求者id
     * @param operaType 操作类型, 参照
     * @return
     */
    @PostMapping("/operatorFriendRequest")
    public ResponseJSONResult operatorFriendRequest(String acceptUserId,String senderId,Integer operaType){
        // no blank judge
        if(StringUtils.isBlank(acceptUserId)||StringUtils.isBlank(senderId)||operaType==null){
            return ResponseJSONResult.errorMsg("blank error");
        }
        //verify whether the operat type exist in OperatorFriendRequestTypeEnum
        if(StringUtils.isBlank(OperatorFriendRequestTypeEnum.getMsgByType(operaType))){
            return ResponseJSONResult.errorMsg("");
        }
        if(operaType==OperatorFriendRequestTypeEnum.IGNORE.getType()){
            userService.deleteFriendRequest(senderId, acceptUserId);
        }else{
            userService.passFriendRequest(senderId, acceptUserId);
        }
        return ResponseJSONResult.ok();
    }

    /**
     * 查询我的好友
     * @param userId
     * @return
     */
    @PostMapping("/queryMyFriends")
    public ResponseJSONResult queryMyFriends(String userId){
        if(StringUtils.isBlank(userId)){
            return ResponseJSONResult.errorMsg("");
        }
        //return the all friends list of user
        List<MyFriendsVo> myFriendsVos=userService.queryMyFriends(userId);
        return ResponseJSONResult.ok(myFriendsVos);
    }
    /**
     * 查询未读的消息
     * @param acceptUserId
     * @return
     */
    @PostMapping("/queryUnreadMsg")
    public ResponseJSONResult queryUnreadMsg(String acceptUserId){
        // 不需要我自己的id因为可以通过数据库查找通讯录得到
        if(StringUtils.isBlank(acceptUserId)) return ResponseJSONResult.errorMsg("blank error");
        // 通过数据库查询unread msg
        List<ChatMsg> unreadMsg=userService.queryUnReadMsg(acceptUserId);
        return ResponseJSONResult.ok(unreadMsg);
    }
}
