package com.dizzia.wordquizzle.server;

import com.dizzia.wordquizzle.commons.WQSettings;
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
    public static ServerHandler server;
    static Database db;


    public int registerUser(String nickUtente, String password) {
        if(password == null || password.equals(""))
            return StatusCode.EMPTY_PASSWORD;

        int result = db.newUser(nickUtente, password);

        if(result == StatusCode.OK)
            try {
                ServerHandler.serialize();
            } catch (IOException e) {
                e.printStackTrace();
                return StatusCode.GENERIC_ERROR;
            }

        return result;
    }

    private static void deserialize() throws IOException {
        FileReader file = new FileReader("./backup.json");
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(file);
        db = gson.fromJson(reader, new TypeToken<Database>(){}.getType());
        file.close();
    }



    public static void main(String[] args) {
        System.setProperty("java.security.policy", "security.policy");
        System.setProperty("java.rmi.server.hostname", WQSettings.RMI_IP);

        try{
            deserialize();
        } catch (IOException e){
            db = new Database();
        }

        try {
            WQServer obj = new WQServer();
            RegisterInterface stub = (RegisterInterface) UnicastRemoteObject.exportObject(obj, STUB_PORT);

            Registry registry = LocateRegistry.createRegistry(REG_PORT);
            registry.rebind(WQSettings.RMI_ADDRESS, stub);

        } catch (RemoteException e) {
            e.printStackTrace();
        }

        server = new ServerHandler(db);
        Thread thread = new Thread(server);
        thread.start();

    }


}
