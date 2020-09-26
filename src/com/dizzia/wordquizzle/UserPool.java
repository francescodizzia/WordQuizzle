package com.dizzia.wordquizzle;

import com.dizzia.wordquizzle.Exceptions.UserAlreadyTakenException;

import java.util.concurrent.ConcurrentHashMap;

public class UserPool {
    //Username e password
    private final ConcurrentHashMap<String,String> userDatabase;

    public UserPool(){
        userDatabase = new ConcurrentHashMap<>();
    }


    public String addUser(String username, String password) throws UserAlreadyTakenException {
        if(password == null)
                return "Password vuota.";

        if(userDatabase.containsKey(username))
           throw new UserAlreadyTakenException();

        userDatabase.put(username, password);
        return "Utente '" + username + "' registrato con successo!";
    }

    public String getRegisteredUsers(){
        return userDatabase.keySet().toString();
    }

}
