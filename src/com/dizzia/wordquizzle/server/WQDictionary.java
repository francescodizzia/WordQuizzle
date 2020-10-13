package com.dizzia.wordquizzle.server;

import javax.swing.plaf.synth.SynthDesktopIconUI;
import java.io.File;
import java.io.FileNotFoundException;
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

//
//    public Vector<String> getWords(int K){
//        Random r = new Random();
//        Vector<String> randomWords = new Vector<>();
//        int random;
//        for(int k=K; k > 0; k--) {
//            random = r.nextInt(WORDS - 1) + 1;
//            randomWords.add(dictionary.get(random));
//        }
//
//        return randomWords;
//    }


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

    public static void main(String[] args){
        WQDictionary d = new WQDictionary();
        Vector<String> words = d.getDistinctWords(5);
        System.out.println(words);

        for(String w: words){
            System.out.println(TranslatorService.getTranslatedWord(w));
        }
    }


}


