package com.ericxxt.pojo.bo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


/**
 * Users的BO对象,用于前端返回数据给后端
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UsersBo {
    private String userId;
    private String faceData;
    private String nickname;

}