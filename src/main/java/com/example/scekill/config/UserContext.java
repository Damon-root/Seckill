package com.example.scekill.config;


import com.example.scekill.pojo.User;

import javax.jws.soap.SOAPBinding;

public class UserContext {
    private  static  ThreadLocal<User> userHolder = new ThreadLocal<User>();
    public static void setUser(User user){
        userHolder.set(user);
    }

    public static User getUser(){
        return userHolder.get();
    }
}
