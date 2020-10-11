package com.dizzia.wordquizzle.database;

import com.dizzia.wordquizzle.commons.StatusCode;
import com.dizzia.wordquizzle.commons.exceptions.UserAlreadyTakenException;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
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

//        public ConcurrentHashMap<String, User> getUserTable(){
//            return userTable;
//        }


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

//            userGraph.get(usernameA).add(userTable.get(usernameB));
//            userGraph.get(usernameB).add(userTable.get(usernameA))

          userGraph.get(usernameA).add(usernameB);
          userGraph.get(usernameB).add(usernameA);
        }



        public String getFriendList(String username){
            //return new ArrayList<>(userGraph.get(username)).toString();
            List<String> list = new ArrayList<String>(userGraph.get(username));
            Gson gson = new Gson();
            return gson.toJson(list);
        }

        public boolean isFriendWith(String usernameA, String usernameB) {
            return userGraph.get(usernameA).contains(usernameB);
        }

        public int getScore(String username){
            return userTable.get(username).getScore();
        }

        public void updateScore(String username, int newScore){
            userTable.get(username).updateScore(newScore);
        }

/*
        public void removeVertex(T v) {
            if (!this.adjacencyList.containsKey(v)) {
                throw new IllegalArgumentException("Vertex doesn't exist.");
            }

            this.adjacencyList.remove(v);

            for (T u: this.getAllVertices()) {
                this.adjacencyList.get(u).remove(v);
            }
        }



        public void removeEdge(T v, T u) {
            if (!this.adjacencyList.containsKey(v) || !this.adjacencyList.containsKey(u)) {
                throw new IllegalArgumentException();
            }

            this.adjacencyList.get(v).remove(u);
            this.adjacencyList.get(u).remove(v);
        }

        public boolean isAdjacent(T v, T u) {
            return this.adjacencyList.get(v).contains(u);
        }

        public Iterable<T> getNeighbors(T v) {
            return this.adjacencyList.get(v);
        }

        public Iterable<T> getAllVertices() {
            return this.adjacencyList.keySet();
        }
 */

    }


