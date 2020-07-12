package com.ericxxt.netty;

import io.netty.channel.Channel;

import java.util.HashMap;
import java.util.Map;

public class UserChannelRelation {
    // define a static HashMap
    private static HashMap<String, Channel> manager=new HashMap<>();

    // put method
    public static void put(String senderId,Channel channel){
        manager.putIfAbsent(senderId, channel);
    }
    // get method
    public static Channel get(String senderId){
        return manager.get(senderId);
    }

    // print the information of all users and channels
    public static void output(){
        for(Map.Entry<String,Channel> channelEntry: manager.entrySet()){
            System.out.println("user:"+channelEntry.getKey()+",channelId:"+channelEntry.getValue().id().asLongText());
        }
    }
}
