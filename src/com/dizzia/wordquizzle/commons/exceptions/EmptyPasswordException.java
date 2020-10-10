package com.dizzia.wordquizzle.commons.exceptions;

public class EmptyPasswordException extends Exception{
    public EmptyPasswordException(){
        super("Empty password");
    }
}
