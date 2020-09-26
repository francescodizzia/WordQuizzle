package com.dizzia.wordquizzle.CLI;

import com.dizzia.wordquizzle.Exceptions.UserAlreadyTakenException;
import com.dizzia.wordquizzle.RegisterInterface;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.MissingFormatArgumentException;
import java.util.Scanner;

public class CLIHandler {
    static String[] args;

    private static boolean compareCMD(String cmd, int min_args){
     if(args.length < min_args) throw new MissingFormatArgumentException("KEKW");

     return args[0].equalsIgnoreCase(cmd);
    }


    public static void executeCMD(String cmd) throws RemoteException, NotBoundException, UserAlreadyTakenException {
        args = cmd.split(" ");

        if(compareCMD("REGISTER", 3)) {
            Registry registry = LocateRegistry.getRegistry(RegisterInterface.REG_PORT);
            RegisterInterface stub = (RegisterInterface) registry.lookup("WQ" + RegisterInterface.MATRICOLA);


            String response = stub.registra_utente(args[1], args[2]);
            System.out.println("response: " + response);
        }
        else if(compareCMD("LOGIN", 3)){
            ;
        }

    }
}
