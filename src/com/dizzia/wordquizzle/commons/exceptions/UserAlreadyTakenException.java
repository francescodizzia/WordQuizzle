package com.dizzia.wordquizzle.commons.exceptions;

public class UserAlreadyTakenException extends Exception{
    public UserAlreadyTakenException(){
        super("User already taken, try a different one");
    }
}
