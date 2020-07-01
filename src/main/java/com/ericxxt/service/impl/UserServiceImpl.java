package com.ericxxt.service.impl;


import com.ericxxt.mapper.UsersMapper;
import com.ericxxt.pojo.Users;
import com.ericxxt.service.UserService;
import com.ericxxt.utils.FastDFSClient;
import com.ericxxt.utils.FileUtils;
import com.ericxxt.utils.QRCodeUtils;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import tk.mybatis.mapper.entity.Example;

import java.io.File;
import java.io.IOException;

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
    //here this method couldn't be autowired because no implementable usersMapper
    public UserServiceImpl(UsersMapper usersMapper,Sid sid,QRCodeUtils qrCodeUtils,FastDFSClient fastDFSClient){
        this.usersMapper=usersMapper;
        this.sid=sid;
        this.qrCodeUtils=qrCodeUtils;
        this.fastDFSClient=fastDFSClient;
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

    //查询最新的用户信息
    private Users queryUsersInfo(String userId){
        return usersMapper.selectByPrimaryKey(userId);
    }
//    @Override
//    public Users updataUsersInfo(Users users) {
//        return null;
//    }
//
//    @Override
//    public Integer preSearchFriends(String myUserId, String username) {
//        return null;
//    }
//
//    @Override
//    public Users queryUserByUsername(String username) {
//        return null;
//    }
//
//    @Override
//    public void sendAddFriendRequest(String myUserId, String username) {
//
//    }
}
