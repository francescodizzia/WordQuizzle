package com.dizzia.wordquizzle.database;

import com.dizzia.wordquizzle.commons.StatusCode;
import com.google.gson.Gson;

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


        public int newUser(String username, String password) {
            User user = new User(username, password);
            userTable.put(username, user);

            if (userGraph.containsKey(username))
                return StatusCode.USER_ALREADY_REGISTERED;

            userGraph.put(username, new HashSet<>());
            return StatusCode.OK;
        }


        public int makeFriends(String usernameA, String usernameB) {
          if(usernameA.equals(usernameB))
              return StatusCode.SELF_REQUEST;

          if(!userGraph.containsKey(usernameA) || !userGraph.containsKey(usernameB)) {
                return StatusCode.USER_NOT_FOUND;
          }

          userGraph.get(usernameA).add(usernameB);
          userGraph.get(usernameB).add(usernameA);
          return StatusCode.OK;
        }


        public String getLeaderboard(String username){
            List<String> friendlist = new ArrayList<>(userGraph.get(username));
            List<LeaderboardPair> board = new ArrayList<>();
            Gson gson = new Gson();

            board.add(new LeaderboardPair(username, getScore(username)));

            for(String friend: friendlist)
                board.add(new LeaderboardPair(friend, getScore(friend)));

            board.sort((o1, o2) -> {
                if (o1.getScore() > o2.getScore())
                    return -1;
                else if (o1.getScore() < o2.getScore())
                    return 1;
                return 0;
            });

            return gson.toJson(board);
        }


        public String getFriendList(String username){
            Gson gson = new Gson();
            List<String> list = new ArrayList<>(userGraph.get(username));
            return gson.toJson(list);
        }


        public int getScore(String username){
            return userTable.get(username).getScore();
        }

        public void updateScore(String username, int newScore){
            userTable.get(username).updateScore(newScore);
        }


    }


