package com.ericxxt.controller;

import com.ericxxt.pojo.Users;
import com.ericxxt.pojo.bo.UsersBo;
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
    @PostMapping("/setNickname")
    public ResponseJSONResult setNickname(@RequestBody UsersBo usersBo){
        Users users=new Users();
        users.setId(usersBo.getUserId());
        users.setNickname(usersBo.getNickname());
        Users usersInfo=userService.updataUsersInfo(users);
        return ResponseJSONResult.ok(usersInfo);
    }
}
