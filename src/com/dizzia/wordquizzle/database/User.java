package com.dizzia.wordquizzle.database;


public class User {

    private final String username;
    private final String password;


    public User(String username, String password) {
        this.username = username.toLowerCase();
        this.password = password;
    }


    public String getUsername(){
        return username;
    }

    public String getPassword(){
        return password;
    }


/*
    @Override
    public boolean equals(Object o){
        if(o instanceof User){
            return username.equalsIgnoreCase(((User)o).username);
        } else{
            return false;
        }
    }



    @Override
    public int hashCode(){
            return username.hashCode();
    }
*/

    }

