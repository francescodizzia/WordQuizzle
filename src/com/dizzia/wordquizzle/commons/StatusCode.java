package com.dizzia.wordquizzle.commons;

public interface StatusCode {
    public final int OK = 200;
    public final int USER_NOT_FOUND = 404;
    public final int EMPTY_PASSWORD = -1;
    public final int USER_ALREADY_REGISTERED = -2;
    public final int WRONG_PASSWORD = -3;

}
