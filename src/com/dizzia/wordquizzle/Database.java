package com.dizzia.wordquizzle;

import com.dizzia.wordquizzle.Exceptions.UserAlreadyTakenException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


public class Database {
        //Grafo delle amicizie
        private final ConcurrentHashMap<String, Set<User>> userGraph;

        //Tabella contenente le info di tutti gli utenti
        private final ConcurrentHashMap<String, User> userTable;

        public Database() {
            userGraph = new ConcurrentHashMap<>();
            userTable = new ConcurrentHashMap<>();
        }

        public ConcurrentHashMap<String, User> getUserTable(){
            return userTable;
        }


        public void newUser(String username, String password) throws UserAlreadyTakenException {
            User user = new User(username, password);
            userTable.put(username, user);

            if (userGraph.containsKey(username)) {
                throw new UserAlreadyTakenException();
            }

            userGraph.put(username, new HashSet<User>());
        }


        public void makeFriends(String usernameA, String usernameB) {
          if (!userGraph.containsKey(usernameA) || !userGraph.containsKey(usernameB)) {
                throw new IllegalArgumentException();
            }

            userGraph.get(usernameA).add(userTable.get(usernameB));
            userGraph.get(usernameB).add(userTable.get(usernameA));
        }



        public ArrayList<String> getFriendList(String username){
            ArrayList<String> friendList = new ArrayList<>();
            Set<User> userSet = userGraph.get(username);

            for(User u: userSet)
                friendList.add(u.getUsername());

            return friendList;
        }

        public boolean isFriendWith(String usernameA, String usernameB) {
            return userGraph.get(usernameA).contains(userTable.get(usernameB));
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


