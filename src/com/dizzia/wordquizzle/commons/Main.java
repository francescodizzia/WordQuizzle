package com.dizzia.wordquizzle.commons;

import com.dizzia.wordquizzle.Exceptions.UserAlreadyTakenException;
import com.dizzia.wordquizzle.UserTable;
import com.dizzia.wordquizzle.UsersGraph;

public class Main {
    public static void main(String[] args) throws UserAlreadyTakenException {
        UserTable table = new UserTable();
        UsersGraph graph = new UsersGraph(table);

        graph.newUser("ciccio", "spyro");
        graph.newUser("stefano", "pane");
        graph.newUser("peppe", "sentry");

        graph.makeFriends("ciccio", "peppe");
        graph.makeFriends("peppe", "stefano");


        System.out.println(graph.getFriendList("peppe"));
        System.out.println(graph.isFriendWith("ciccio", "stefano"));
        graph.makeFriends("stefano", "ciccio");
        System.out.println(graph.isFriendWith("ciccio", "stefano"));

    }
}
