package com.dizzia.wordquizzle.Exceptions;

public class UserAlreadyTakenException extends Exception{
    public UserAlreadyTakenException(){
        super("User already taken, try a different one");
    }
}
