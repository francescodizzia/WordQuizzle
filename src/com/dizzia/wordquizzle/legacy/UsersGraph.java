//package com.dizzia.wordquizzle.legacy;
//
//import com.dizzia.wordquizzle.Exceptions.UserAlreadyTakenException;
//import com.dizzia.wordquizzle.database.User;
//
//import java.util.ArrayList;
//import java.util.HashSet;
//import java.util.Set;
//import java.util.concurrent.ConcurrentHashMap;
//
//
//public class UsersGraph {
//        final private ConcurrentHashMap<String, Set<User>> adjacencyList;
//        private final UserTable table;
//
//        public UsersGraph(UserTable table) {
//            this.adjacencyList = new ConcurrentHashMap<>();
//            this.table = table;
//        }
//
//        public UserTable getUserTable(){
//            return this.table;
//        }
//
//
//        public void addUser(String username, String password) throws UserAlreadyTakenException {
//            User user = table.addUser(username, password);
//
//            if (this.adjacencyList.containsKey(username)) {
//                throw new UserAlreadyTakenException();
//            }
//
//            this.adjacencyList.put(username, new HashSet<User>());
//        }
//
//        public void makeFriends(String usernameA, String usernameB) {
//          if (!this.adjacencyList.containsKey(usernameA) || !this.adjacencyList.containsKey(usernameB)) {
//                throw new IllegalArgumentException();
//            }
//
//            this.adjacencyList.get(usernameA).add(table.getUser(usernameB));
//            this.adjacencyList.get(usernameB).add(table.getUser(usernameA));
//        }
//
//
//
//
//        public ArrayList<String> getFriendList(String username){
//            ArrayList<String> result = new ArrayList<>();
//            Set<User> setUser = this.adjacencyList.get(username);
//            for(User u: setUser){
//                result.add(u.getUsername());
//            }
//
//            return result;
//        }
//
//        public boolean isFriendWith(String usernameA, String usernameB) {
//            return this.adjacencyList.get(usernameA).contains(table.getUser(usernameB));
//        }
//
//
///*
//        public void removeVertex(T v) {
//            if (!this.adjacencyList.containsKey(v)) {
//                throw new IllegalArgumentException("Vertex doesn't exist.");
//            }
//
//            this.adjacencyList.remove(v);
//
//            for (T u: this.getAllVertices()) {
//                this.adjacencyList.get(u).remove(v);
//            }
//        }
//
//
//
//        public void removeEdge(T v, T u) {
//            if (!this.adjacencyList.containsKey(v) || !this.adjacencyList.containsKey(u)) {
//                throw new IllegalArgumentException();
//            }
//
//            this.adjacencyList.get(v).remove(u);
//            this.adjacencyList.get(u).remove(v);
//        }
//
//        public boolean isAdjacent(T v, T u) {
//            return this.adjacencyList.get(v).contains(u);
//        }
//
//        public Iterable<T> getNeighbors(T v) {
//            return this.adjacencyList.get(v);
//        }
//
//        public Iterable<T> getAllVertices() {
//            return this.adjacencyList.keySet();
//        }
// */
//
//    }
//
//
