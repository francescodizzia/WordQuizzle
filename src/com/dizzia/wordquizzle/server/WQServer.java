package com.dizzia.wordquizzle.server;

import com.dizzia.wordquizzle.commons.WQSettings;
import com.dizzia.wordquizzle.commons.exceptions.UserAlreadyTakenException;
import com.dizzia.wordquizzle.RegisterInterface;
import com.dizzia.wordquizzle.commons.StatusCode;
import com.dizzia.wordquizzle.database.Database;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.FileReader;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class WQServer implements RegisterInterface {
    static ServerHandler server;
    static Database db;


    public int registerUser(String nickUtente, String password) {
        if(password == null || password.equals(""))
            return StatusCode.EMPTY_PASSWORD;

        try {
            db.newUser(nickUtente, password);
        } catch (UserAlreadyTakenException e) {
            return StatusCode.USER_ALREADY_REGISTERED;
        }
        try {
            ServerHandler.serialize();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return StatusCode.OK;
    }

    private static void deserialize() throws IOException {
        FileReader file = new FileReader("./backup.json");
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(file);
        db = gson.fromJson(reader, new TypeToken<Database>(){}.getType());
        file.close();
    }



    public static void main(String[] args) throws IOException {
        try{
            deserialize();
        } catch (IOException e){
            db = new Database();
        }


        System.setProperty("java.security.policy", "security.policy");
        System.setProperty("java.rmi.server.hostname", WQSettings.RMI_IP);

        if (System.getSecurityManager() == null) {
            System.setSecurityManager(new SecurityManager());
        }


        try {
            WQServer obj = new WQServer();
            RegisterInterface stub = (RegisterInterface) UnicastRemoteObject.exportObject(obj, STUB_PORT);

            Registry registry = LocateRegistry.createRegistry(REG_PORT);
            registry.rebind(WQSettings.RMI_ADDRESS, stub);
            //registry.rebind("WordQuizzle_" + MATRICOLA, stub);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        server = new ServerHandler(db);
        Thread thread = new Thread(server);
        thread.start();


//        System.setProperty("java.rmi.server.hostname","79.42.92.249");



    }


}
