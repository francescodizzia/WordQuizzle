package com.dizzia.wordquizzle.database;

import com.dizzia.wordquizzle.commons.StatusCode;
import com.dizzia.wordquizzle.commons.exceptions.UserAlreadyTakenException;
import com.google.gson.Gson;
import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;


public class Database {
        //Grafo delle amicizie
        private final ConcurrentHashMap<String, HashSet<String>> userGraph;

        //Tabella contenente le info di tutti gli utenti
        private final ConcurrentHashMap<String, User> userTable;

        public Database() {
            userGraph = new ConcurrentHashMap<>();
            userTable = new ConcurrentHashMap<>();
        }


        public int checkCredentials(String username, String password){
            User user = userTable.get(username);

            if(user == null){
                return StatusCode.USER_NOT_FOUND;
            }else if(user.getPassword().equals(password)){
                return StatusCode.OK;
            }

            return StatusCode.WRONG_PASSWORD;
        }


        public void newUser(String username, String password) throws UserAlreadyTakenException {
            User user = new User(username, password);
            userTable.put(username, user);

            if (userGraph.containsKey(username)) {
                throw new UserAlreadyTakenException();
            }

            userGraph.put(username, new HashSet<>());
        }


        public void makeFriends(String usernameA, String usernameB) {
          if(!userGraph.containsKey(usernameA) || !userGraph.containsKey(usernameB)) {
                throw new IllegalArgumentException();
          }

          userGraph.get(usernameA).add(usernameB);
          userGraph.get(usernameB).add(usernameA);
        }


        public String getLeaderboard(String username){
            List<String> friendlist = new ArrayList<>(userGraph.get(username));
            List<Pair<String, Integer>> board = new ArrayList<>();

            for(String friend: friendlist)
                board.add(new Pair<>(friend, getScore(friend)));


            board.sort((o1, o2) -> {
                if (o1.getValue() > o2.getValue())
                    return -1;
                else if (o1.getValue() < o2.getValue())
                    return 1;
                return 0;
            });

            Gson gson = new Gson();
            return gson.toJson(board);
        }


        public String getFriendList(String username){
            List<String> list = new ArrayList<>(userGraph.get(username));
            Gson gson = new Gson();
            return gson.toJson(list);
        }


        public int getScore(String username){
            return userTable.get(username).getScore();
        }

        public void updateScore(String username, int newScore){
            userTable.get(username).updateScore(newScore);
        }


    }


