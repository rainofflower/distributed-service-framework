package com.yanghui.distributed.framework.example;

import lombok.Data;

/**
 * @author YangHui
 */
@Data
public class User {
    String name;
    Integer age;

    public User(){}

    public User(String name, int age){
        this.name = name;
        this.age = age;
    }

}
