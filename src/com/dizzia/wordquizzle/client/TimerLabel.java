package com.dizzia.wordquizzle.client;

import javax.swing.*;

public class TimerLabel extends JLabel {
    public int remainingTime = 80;
    Timer timer;

    public TimerLabel(JFrame frame) {
        timer = new Timer(1000, e -> {
            remainingTime--;
            repaint();

            if(remainingTime == 0){
                timer.stop();
                frame.dispose();
            }
        });
        timer.start();
    }

    public String getRemainingTime() {
        int minutes = remainingTime / 60;
        int seconds = remainingTime % 60;

        return (minutes + ":" + seconds);
    }

    @Override
    public String getText() {
        return getRemainingTime();
    }
}