package com.dizzia.wordquizzle.client;

import com.dizzia.wordquizzle.commons.WQSettings;
import javax.swing.*;

//Label particolare che funge anche da Timer:
//ogni secondo si aggiorna, scala un secondo dal timer,
//viene ridisegnato sulla GUI e al raggiungimento dello
//zero chiude la finestra e gestisce la conclusione della
//partita
public class TimerLabel extends JLabel {
    public int remainingTime = WQSettings.CHALLENGE_TIMEOUT / 1000;
    Timer timer;

    public TimerLabel(JFrame frame) {
        timer = new Timer(1000, e -> {
            remainingTime--;
            repaint();

            if(WQClient.endgame){
                timer.stop();
                return;
            }


            if(remainingTime == 0){
                timer.stop();
                frame.dispose();

                if(!WQClient.endgame){
                    WaitingDialog waitingDialog = new WaitingDialog();
                    WQClient.waitEnd(waitingDialog);
                }
            }
        });
        timer.start();
    }

    public String getRemainingTime() {
        int minutes = remainingTime / 60;
        int seconds = remainingTime % 60;

        return minutes + ":" + seconds;
    }

    @Override
    public String getText() {
        return getRemainingTime();
    }
}