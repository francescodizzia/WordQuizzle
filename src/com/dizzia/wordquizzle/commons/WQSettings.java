package com.dizzia.wordquizzle.commons;

import javax.swing.*;
import java.awt.*;


public interface WQSettings {
    int N_WORDS = 8;
    int UNIT = 1000;
    int CHALLENGE_TIMEOUT = 60 * UNIT;
    int CHALLENGE_REQUEST_TIMEOUT = 8 * UNIT;

    int RIGHT_ANSWER_POINTS = 2;
    int WRONG_ANSWER_POINTS = -1;
    int WINNER_EXTRA_POINTS = 3;


    String HOSTNAME = "localhost";
//    int TCP_PORT = 1919;
    int TCP_PORT = 54410;
    String RMI_ADDRESS = "rmi://" + HOSTNAME + "/WordQuizzle_544107";


}
