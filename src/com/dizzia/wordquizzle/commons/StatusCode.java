package com.dizzia.wordquizzle.commons;

public interface StatusCode {
    int OK = 200;
    int GENERIC_ERROR = -1;
    int USER_NOT_FOUND = -2;
    int EMPTY_PASSWORD = -3;
    int USER_ALREADY_REGISTERED = -4;
    int USER_ALREADY_LOGGED = -5;
    int WRONG_PASSWORD = -6;
    int SELF_REQUEST = -7;
    int BUSY_FRIEND = -8;
}
