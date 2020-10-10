package com.dizzia.wordquizzle;

import java.io.Serializable;
import java.util.concurrent.ConcurrentHashMap;

public class UserTable implements Serializable {
    private final ConcurrentHashMap<String, User> userTable;

    public UserTable(){
        userTable = new ConcurrentHashMap<>();
    }

    public User newUser(String username, String password){
        User user = new User(username, password);
        userTable.put(username, user);
        return user;
    }

    public User getUser(String username){
        User user = this.userTable.get(username);
        if(user == null) return null;
        return userTable.get(username);
    }

    public boolean isUserRegistered(String username){
        return this.userTable.containsKey(username);
    }

}
