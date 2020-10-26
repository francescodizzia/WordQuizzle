package com.dizzia.wordquizzle.server;

import com.dizzia.wordquizzle.commons.WQSettings;
import com.dizzia.wordquizzle.commons.RegisterInterface;
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
    private static Database db;

    //Metodo remoto che registra l'utente al servizio
    public int registra_utente(String nickUtente, String password) {
        //Se la password è nulla termino l'operazione e ritorno il relativo
        //codice d'errore
        if(password == null || password.equals(""))
            return StatusCode.EMPTY_PASSWORD;

        //Provo ad aggiungere l'utente al database
        int result = db.newUser(nickUtente, password);

        //Se l'operazione è andata a buon fine procedo serializzando il file JSON,
        //altrimenti restituisco errore
        if(result == StatusCode.OK)
            try {
                ServerHandler.serialize();
            } catch (IOException e) {
                e.printStackTrace();
                return StatusCode.GENERIC_ERROR;
            }
        return result;
    }

    //Metodo che deserializza il file JSON contenente il database degli utenti
    private static void deserialize() throws IOException {
        //Carico il file in lettura e vado a ricreare la classe database
        //con i contenuti del file JSON
        FileReader file = new FileReader("./backup.json");
        Gson gson = new Gson();
        JsonReader reader = new JsonReader(file);
        db = gson.fromJson(reader, new TypeToken<Database>(){}.getType());
        file.close();
    }



    public static void main(String[] args) {

        System.setProperty("java.security.policy", "security.policy");
        System.setProperty("java.rmi.server.hostname", WQSettings.HOSTNAME);

        //Deserializzo il database JSON e lo carico, se invece non riesco a
        //caricarlo (magari perché non esiste il file) ne inizializzo uno
        //nuovo
        try{
            deserialize();
        } catch (IOException e){
            db = new Database();
        }


        //Esporto l'oggetto remoto e inizializzo il necessario per RMI
        try {
            WQServer obj = new WQServer();
            RegisterInterface stub = (RegisterInterface) UnicastRemoteObject.exportObject(obj, STUB_PORT);
            Registry registry = LocateRegistry.createRegistry(REG_PORT);
            registry.rebind(WQSettings.RMI_ADDRESS, stub);
        } catch (RemoteException e) {
            e.printStackTrace();
        }


        //Lancio il ServerHandler
        server = new ServerHandler(db);
        Thread thread = new Thread(server);
        thread.start();
    }


}
