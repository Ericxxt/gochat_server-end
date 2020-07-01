package com.ericxxt.pojo;


import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@Table(name = "users")
public class Users implements Serializable {
    /**
     * ID
     */
    @Id
    @Column(name = "id")
    private String id;

    /**
     * 用户名
     */
    @Column(name = "username")
    private String username;

    /**
     * 密码，已加密
     */
    @Column(name = "`password`")
    private String password;

    /**
     * 用户头像
     */
    @Column(name = "face_image")
    private String faceImage;

    /**
     * 用户大头像
     */
    @Column(name = "face_image_big")
    private String faceImageBig;

    /**
     * 用户昵称
     */
    @Column(name = "nickname")
    private String nickname;

    /**
     * 用户二维码
     */
    @Column(name = "qrcode")
    private String qrcode;

    /**
     * 用户客户端id
     */
    @Column(name = "cid")
    private String cid;

    private static final long serialVersionUID = 1L;
}

//lombok  常用
//Val 可以将变量申明是final类型
//@NonNull 能够为方法或构造函数的参数提供非空检查
//@Cleanup 能够自动释放资源
//@Getter/@Setter 可以针对类的属性字段自动生成Get/Set方法
//@ToString 使用该注解的类生成一个toString方法
//@EqualsAndHashCode 使用该注解的类自动生成equals和hashCode方法
//@NoArgsConstructor, @RequiredArgsConstructor, @AllArgsConstructor,这几个注解分别为类自动生成了无参构造器、指定参数的构造器和包含所有参数的构造器。
//@Data注解作用比较全，其包含注解的集合@ToString，@EqualsAndHashCode，所有字段的@Getter和所有非final字段的@Setter, @RequiredArgsConstructor等
//@Builder注解提供了一种比较推崇的构建值对象的方式
//@Synchronized注解类似Java中的Synchronized 关键字，但是可以隐藏同步锁