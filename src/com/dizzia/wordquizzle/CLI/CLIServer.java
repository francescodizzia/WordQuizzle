package com.dizzia.wordquizzle.CLI;

import com.dizzia.wordquizzle.UserPool;

import java.util.Scanner;

public class CLIServer implements Runnable{
    private final UserPool userpool;

    public CLIServer(UserPool userpool){
        this.userpool = userpool;
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
                System.out.println(userpool.getRegisteredUsers());
            else break;
        }

        System.exit(0);
    }
}
