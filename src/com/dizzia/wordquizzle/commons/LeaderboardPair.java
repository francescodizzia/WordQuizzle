package com.dizzia.wordquizzle.commons;

//Classe contenente le coppie <username, score>, utile per la classifica degli utenti
public class LeaderboardPair {
    private final String username;
    private final int score;

    public LeaderboardPair(String username, int score) {
        this.username = username;
        this.score = score;
    }

    public int getScore() {
        return score;
    }

    public String getUsername() {
        return username;
    }
}
