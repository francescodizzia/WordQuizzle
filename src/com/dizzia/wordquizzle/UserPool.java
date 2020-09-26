package com.dizzia.wordquizzle;

import com.dizzia.wordquizzle.Exceptions.UserAlreadyTakenException;

import java.util.concurrent.ConcurrentHashMap;

public class UserPool {
    //Username e password
    private final ConcurrentHashMap<String, User> userDatabase;

    public UserPool(){
        userDatabase = new ConcurrentHashMap<>();
    }


    public String addUser(String username, String password) throws UserAlreadyTakenException {
        if(password == null)
                return "Password vuota.";

        if(userDatabase.containsKey(username))
           throw new UserAlreadyTakenException();

        User user = new User(password);
        userDatabase.put(username, user);
        return "Utente '" + username + "' registrato con successo!";
    }

    public ConcurrentHashMap<String, User> getRegisteredUsers(){
        return userDatabase;
    }

}
