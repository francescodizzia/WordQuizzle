package com.dizzia.wordquizzle.commons;

public interface WQSettings {
    //Numero di parole da tradurre
    int N_WORDS = 8;

    //Un secondo espresso in millisecondi
    int UNIT = 1000;

    //Timer della sfida
    int CHALLENGE_TIMEOUT = 60 * UNIT;

    //Timeout richiesta di sfida
    int CHALLENGE_REQUEST_TIMEOUT = 15 * UNIT;

    //Punteggi assegnati durante la sfida
    int RIGHT_ANSWER_POINTS = 2;
    int WRONG_ANSWER_POINTS = -1;
    int WINNER_EXTRA_POINTS = 3;

    String HOSTNAME = "localhost";
    int TCP_PORT = 54410;
    String RMI_ADDRESS = "rmi://" + HOSTNAME + "/WordQuizzle_544107";
}
