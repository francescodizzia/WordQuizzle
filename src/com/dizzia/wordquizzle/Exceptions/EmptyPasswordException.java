package com.dizzia.wordquizzle.Exceptions;

public class EmptyPasswordException extends Exception{
    public EmptyPasswordException(){
        super("Empty password");
    }
}
