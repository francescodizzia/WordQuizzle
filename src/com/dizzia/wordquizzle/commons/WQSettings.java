package com.dizzia.wordquizzle.commons;

import com.dizzia.wordquizzle.RegisterInterface;


public interface WQSettings {
    int N_WORDS = 5;
    int CHALLENGE_TIMEOUT = 60;
    int RIGHT_ANSWER_POINTS = 2;
    int WRONG_ANSWER_POINTS = -1;
    int WINNER_EXTRA_POINTS = 3;
    String RMI_IP = "79.42.92.249";
    String RMI_ADDRESS = "rmi://" + RMI_IP + "/WordQuizzle_" + RegisterInterface.MATRICOLA;
}
