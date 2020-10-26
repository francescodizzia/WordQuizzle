package com.dizzia.wordquizzle.server;

//Classe che rappresenta una semplice coppia <password, score>
public class User {
    private final String password;
    private int score;

    public User(String password) {
        this.password = password;
        score = 0;
    }

    public String getPassword(){
        return password;
    }

    public int getScore(){
        return score;
    }

    public void updateScore(int score){
        this.score = score;
    }

    }

