package com.dizzia.wordquizzle.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javax.swing.plaf.synth.SynthDesktopIconUI;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;

public class WQDictionary {
    private final ArrayList<String> dictionary;
    private int WORDS;

    public WQDictionary() {
        WORDS = 0;
        dictionary = new ArrayList<>();
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



    public Vector<String> getDistinctWords(int N){
        Vector<String> randomWords = new Vector<>();
        HashSet<Integer> randomNumbers = new HashSet<>();
        Random r = new Random();

        while (randomNumbers.size() < N)
            randomNumbers.add(r.nextInt(WORDS - 1) + 1);

        for(Integer i: randomNumbers){
            randomWords.add(dictionary.get(i));
        }

        return randomWords;
    }



    public static ArrayList<String> getTranslatedWords(String word){
        ArrayList<String> words = new ArrayList<>();

        try {
            URL u = new URL("https://api.mymemory.translated.net/get?q=" + URLEncoder.encode(word, "UTF-8") + "&langpair=it|en");
            InputStreamReader reader = new InputStreamReader(u.openStream());

            JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
            JsonArray matches = jsonObject.getAsJsonArray("matches");

            for(JsonElement element: matches)
                words.add(element.getAsJsonObject().getAsJsonPrimitive("translation").getAsString().toLowerCase());


        } catch (IOException e) {
            e.printStackTrace();
        }

        return words;
    }



}


