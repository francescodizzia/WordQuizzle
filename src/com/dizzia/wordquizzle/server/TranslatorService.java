package com.dizzia.wordquizzle.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;

public class TranslatorService {


    public static ArrayList<String> getTranslatedWord(String word){
        ArrayList<String> words = new ArrayList<>();

        try {
            URL u = new URL("https://api.mymemory.translated.net/get?q=" + URLEncoder.encode(word, "UTF-8") + "&langpair=it|en");
            InputStreamReader reader = new InputStreamReader(u.openStream());

//            JsonObject jsonObject = new Gson().fromJson(reader, JsonObject.class);
//            JsonObject respondeData = jsonObject.get("responseData").getAsJsonObject();
//            result = respondeData.get("translatedText").getAsString();

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
