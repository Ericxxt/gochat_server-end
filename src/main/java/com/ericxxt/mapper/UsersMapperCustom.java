package com.ericxxt.mapper;

import com.ericxxt.pojo.Users;
import com.ericxxt.pojo.vo.FriendRequestVo;
import com.ericxxt.pojo.vo.MyFriendsVo;
import com.ericxxt.utils.MyMapper;

import java.util.List;

/**
 * 用于接收添加好友者的信息
 */

public interface UsersMapperCustom extends MyMapper<Users> {

    List<FriendRequestVo> queryFriendRequestList(String acceptId);

    List<MyFriendsVo> queryMyFriends(String myUserId);

    void updateSignMsg(List<String> msgIdList);
}