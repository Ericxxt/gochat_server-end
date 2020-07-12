package com.ericxxt.netty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
public class DataContent implements Serializable {

    // 动作类型，是由枚举类型决定 在MsgActionEnum
    private Integer action;
    //传递过来的消息
    private ChatData chatData;
    //扩展字段
    private String extend;
}
