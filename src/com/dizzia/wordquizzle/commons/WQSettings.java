package com.dizzia.wordquizzle.commons;

import com.dizzia.wordquizzle.RegisterInterface;

import javax.swing.*;
import java.awt.*;


public interface WQSettings {
    int N_WORDS = 5;
    int CHALLENGE_TIMEOUT = 60;
    int RIGHT_ANSWER_POINTS = 2;
    int WRONG_ANSWER_POINTS = -1;
    int WINNER_EXTRA_POINTS = 3;
    //String RMI_IP = "79.42.92.249";
    String RMI_IP = "localhost";
    String RMI_ADDRESS = "rmi://" + RMI_IP + "/WordQuizzle_" + RegisterInterface.MATRICOLA;

    static void applyCustomTheme() throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.put( "control", new Color( 81, 86, 88) );
        UIManager.put( "info", new Color(40,42,54) );
        UIManager.put( "nimbusBase", new Color( 18, 30, 49) );
        UIManager.put( "nimbusAlertYellow", new Color( 248, 187, 0) );
        UIManager.put( "nimbusDisabledText", new Color( 128, 128, 128) );
        UIManager.put( "nimbusFocus", new Color(115,164,209) );
        UIManager.put( "nimbusGreen", new Color(176,179,50) );
        UIManager.put( "nimbusInfoBlue", new Color( 66, 139, 221) );
        UIManager.put( "nimbusLightBackground", new Color( 18, 30, 49) );
        UIManager.put( "nimbusOrange", new Color(191,98,4) );
        UIManager.put( "nimbusRed", new Color(169,46,34) );
        UIManager.put( "nimbusSelectedText", new Color( 255, 255, 255) );
        UIManager.put( "nimbusSelectionBackground", new Color( 104, 93, 156) );
        UIManager.put( "text", new Color( 230, 230, 230) );

        for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                javax.swing.UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }

    }
}
