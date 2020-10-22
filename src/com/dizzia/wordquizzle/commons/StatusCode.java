package com.dizzia.wordquizzle.commons;

public interface StatusCode {
    int OK = 200;
    int USER_NOT_FOUND = 404;
    int EMPTY_PASSWORD = -1;
    int USER_ALREADY_REGISTERED = -2;
    int USER_ALREADY_LOGGED = -3;
    int WRONG_PASSWORD = -4;
    int REFUSED = 400;



}
