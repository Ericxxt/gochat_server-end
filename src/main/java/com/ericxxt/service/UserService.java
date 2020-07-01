package com.ericxxt.service;

import com.ericxxt.pojo.FriendsRequest;
import com.ericxxt.pojo.Users;

import java.util.List;

public interface UserService {

    /**
     * 查询用户名是否存在
     * @param username
     * @return
     */
    //接口的public好像没什么必要
    public boolean queryUsernameIsExist(String username);
    /**
     * 用户登录校验
     * @param username
     * @param pwd
     * @return
     */
    Users queryUserForLogin(String username, String pwd);
    /**
     * 用户注册接口
     * @param user
     * @return
     */
    Users saveUser(Users user);

    /**
     * 更新用户信息
     * @param users
     * @return
     */

    Users updataUsersInfo(Users users);

    /**
     * 搜索朋友的前置条件查询
     * @param myUserId
     * @param username
     * @return
     */

//    Integer preSearchFriends(String myUserId, String username);

    /**
     * 根据用户名查询用户信息
     * @param username
     * @return
     */
//    Users queryUserByUsername(String username);

    /**
     * 发送添加好友请求
     * @param myUserId
     * @param username
     */
//    void sendAddFriendRequest(String myUserId,String username);


//    Users queryUsersInfo(String userId);

}
