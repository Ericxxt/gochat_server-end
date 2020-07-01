package com.ericxxt.pojo.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;



/**
 * Users的VO对象,用于后端返回数据给前端
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsersVo  {
    private String id;
    private String username;
    private String faceImage;
    private String faceImageBig;
    private String nickname;
    private String qrcode;
}