package com.dizzia.wordquizzle.commons;

import javax.swing.*;
import java.awt.*;


public interface WQSettings {
    int N_WORDS = 10;
    int UNIT_SECONDS = 1000;
    int CHALLENGE_TIMEOUT = 100 * UNIT_SECONDS;
    int CHALLENGE_REQUEST_TIMEOUT = 7 * UNIT_SECONDS;

    int RIGHT_ANSWER_POINTS = 2;
    int WRONG_ANSWER_POINTS = -1;
    int WINNER_EXTRA_POINTS = 3;

//    String RMI_IP = "slazard.it";
    String RMI_IP = "localhost";
    String RMI_ADDRESS = "rmi://" + RMI_IP + "/WordQuizzle_544107";



    static void applyCustomTheme() throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.put( "control", new Color(81, 86, 88));
        UIManager.put( "info", new Color(40,42,54));
        UIManager.put( "nimbusBase", new Color(18, 30, 49));
        UIManager.put( "nimbusDisabledText", new Color( 128, 128, 128));
//        UIManager.put( "nimbusFocus", new Color(115,164,209));
//        UIManager.put( "nimbusInfoBlue", new Color(66, 139, 221));
        UIManager.put( "nimbusLightBackground", new Color(18, 30, 49));
        UIManager.put( "nimbusSelectedText", new Color(255, 255, 255));
        UIManager.put( "nimbusSelectionBackground", new Color(104, 93, 156));
        UIManager.put( "text", new Color(230, 230, 230));

        for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                javax.swing.UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }

    }
}
