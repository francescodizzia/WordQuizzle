package com.dizzia.wordquizzle;

import com.dizzia.wordquizzle.Exceptions.UserAlreadyTakenException;
import com.dizzia.wordquizzle.legacy.UserTable;
import com.dizzia.wordquizzle.legacy.UsersGraph;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import java.io.*;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;


public class WQServer implements RegisterInterface{
//    static UserTable table;
//    static UsersGraph graph;
    static ServerHandler server;
    static Database db;


    public void registerUser(String nickUtente, String password) throws RemoteException, UserAlreadyTakenException, NotBoundException {
        db.newUser(nickUtente, password);
        try {
            server.serialize();
        } catch (IOException e) {
            e.printStackTrace();
        }
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


        try {
            WQServer obj = new WQServer();
            RegisterInterface stub = (RegisterInterface) UnicastRemoteObject.exportObject(obj, STUB_PORT);

            Registry registry = LocateRegistry.createRegistry(REG_PORT);
            registry.rebind("WordQuizzle_" + MATRICOLA, stub);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        server = new ServerHandler(db);
        Thread thread = new Thread(server);
        thread.start();

        //System.setProperty("java.rmi.server.hostname","95.248.187.159");



    }


}
