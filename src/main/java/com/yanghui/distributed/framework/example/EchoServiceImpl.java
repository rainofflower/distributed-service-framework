package com.yanghui.distributed.framework.example;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * 服务提供者
 *
 * Created by YangHui on 2019/11/22
 */
public class EchoServiceImpl implements EchoService{

    private AtomicInteger id = new AtomicInteger(0);

    @Override
    public String echo(String ping) {
        return ping != null ? id.incrementAndGet() + ": " + ping + "--> I am ok." : id.incrementAndGet() + ": " + "I am ok.";
    }

    @Override
    public String friend(User user, int low, int high, String nick) {
        return "返回信息："+ user.getName() + " | " + user.getAge() + " | " + low + " - " + high + " | "+nick;
    }

    @Override
    public void oneWayTest(List<User> users, String name) {
        StringBuilder builder = new StringBuilder();
        for(User user : users){
            builder.append(user.getName())
                    .append("-")
                    .append(user.getAge())
                    .append("\n");
        }
        System.out.println("name:"+name+"\n"+builder.toString());
    }

    @Override
    public String test2() {
        String s = "test2执行了业务";
        System.out.println(s);
        return s;
    }

    @Override
    public User getUserByName(String name){
        return new User(name,20);
    }


    @Override
    public List<User> listUser(String name) {
        List<User> list = new ArrayList<>();
        list.add(new User(name,18));
        list.add(new User(name,19));
        list.add(new User(name,20));
        return list;
    }

    @Override
    public List<User> matchUser(List<User> users, String name){
        return users.stream()
                .filter(u -> Objects.equals(u.getName(),name))
                .collect(Collectors.toList());
    }
}
