package com.ericxxt.netty;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatData implements Serializable {
    // sender id
    private String senderId;
    // receiver's id
    private String receiverId;
    // the message part
    private String msg;
    // the id of message, that stored in msg database
    private String msgId;
}
