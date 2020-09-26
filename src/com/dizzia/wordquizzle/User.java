package com.dizzia.wordquizzle;

import java.util.HashSet;

public class User {
    private final String password;
    HashSet<String> friendList;

    public User(String password) {
        this.password = password;
        friendList = new HashSet<>();
    }

    public String getPassword(){
        return password;
    }

    public void addFriend(String friend){
        friendList.add(friend);
    }

    public HashSet<String> getFriends(){
        return friendList;
    }

}
