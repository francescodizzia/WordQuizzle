package com.dizzia.wordquizzle.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;


public class WQDictionary {
    private final Vector<String> dictionary;
    private int WORDS;

    //Creo il dizionario a partire dalle parole
    //contenute nel relativo file
    public WQDictionary() {
        WORDS = 0;
        dictionary = new Vector<>();
        File file = new File("./words.txt");
        try {
            Scanner input = new Scanner(file);
            while (input.hasNextLine()) {
                dictionary.add(input.nextLine());
                WORDS++;
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }


    //Estrae N parole distinte dal dizionario e le inserisce
    //in una nuova lista
    public ArrayList<String> getDistinctWords(int N){
        ArrayList<String> randomWords = new ArrayList<>();
        HashSet<Integer> randomNumbers = new HashSet<>();
        Random r = new Random();

        //Prelevo una parola a caso e la inserisco
        //finché non ho aggiunto N parole
        //(l'unicità di ogni parola è garantita
        //dall'hash set)
        while (randomNumbers.size() < N)
            randomNumbers.add(r.nextInt(WORDS - 1) + 1);

        for(Integer i: randomNumbers)
            randomWords.add(dictionary.get(i));

        return randomWords;
    }


    //Metodo che ritorna una lista di traduzioni possibili per una
    //determinata parola
    public static ArrayList<String> getTranslatedWords(String word){
        ArrayList<String> words = new ArrayList<>();

        //Effettuo una richiesta GET verso l'API di MyMemory
        try {
            URL u = new URL("https://api.mymemory.translated.net/get?q=" + URLEncoder.encode(word, "UTF-8") + "&langpair=it|en");
            InputStreamReader reader = new InputStreamReader(u.openStream());
            JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);

            //Preleva l'array contenente le traduzioni
            JsonArray matches = jsonObject.getAsJsonArray("matches");

            //Aggiunge ogni traduzione alla lista
            for(JsonElement element: matches)
                words.add(element.getAsJsonObject().getAsJsonPrimitive("translation").getAsString().toLowerCase());

        } catch (IOException e) {
            e.printStackTrace();
        }

        return words;
    }



}


