package com.dizzia.wordquizzle.legacy.client;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class Spawner {

    public static void main(String[] args){
        ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();
        for(int i=1; i<6; i++){
            WQClient c = new WQClient();
            Thread t = new Thread(c);
            executor.execute(t);
        }
        executor.shutdown();
    }

}