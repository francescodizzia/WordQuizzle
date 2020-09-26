package com.dizzia.wordquizzle.CLI;

import com.dizzia.wordquizzle.User;
import com.dizzia.wordquizzle.UserPool;

import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ConcurrentHashMap;

public class CLIServer implements Runnable{
    private final UserPool userpool;

    public CLIServer(UserPool userpool){
        this.userpool = userpool;
    }

    public void makeFriends(String user1, String user2){
        userpool.getRegisteredUsers().get(user1).addFriend(user2);
        userpool.getRegisteredUsers().get(user2).addFriend(user1);
    }

    public void run() {
        Scanner s = new Scanner(System.in);
        String input;


    /*
    JFrame frame = new JFrame("GUI_Test");
    frame.setContentPane(new GUI_Test().panel1);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.pack();
    frame.setVisible(true);
    */

        while(true){
            input = s.nextLine();
            if(input.equalsIgnoreCase("g"))
                for(Map.Entry<String, User> record: userpool.getRegisteredUsers().entrySet()){
                    User u = record.getValue();
                    System.out.println("username: " + record.getKey() + " | psw: " + u.getPassword() + " | friends: " + u.getFriends());
                }
            else if(input.equalsIgnoreCase("p")) {
                makeFriends("peppe", "sentry");
            }
            else break;
        }

        System.exit(0);
    }
}
