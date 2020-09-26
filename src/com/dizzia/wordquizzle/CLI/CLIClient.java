package com.dizzia.wordquizzle.CLI;

import com.dizzia.wordquizzle.UserPool;

import java.util.Scanner;

public class CLIClient implements Runnable{
    private final UserPool userpool;

    public CLIClient(UserPool userpool){
        this.userpool = userpool;
    }

    public void run() {
        Scanner s = new Scanner(System.in);
        String input;

        while(true){
            input = s.nextLine();
            if(input.equalsIgnoreCase("g"))
                System.out.println(userpool.getRegisteredUsers());
            else break;
        }

        System.exit(0);
    }
}


