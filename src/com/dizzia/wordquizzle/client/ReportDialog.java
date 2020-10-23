package com.dizzia.wordquizzle.client;

import com.dizzia.wordquizzle.commons.WQSettings;

import javax.swing.*;
import java.awt.*;

public class ReportDialog extends JDialog {
    Container container = getContentPane();
    JLabel wordLabel = new JLabel();


    public ReportDialog(int isWinner, int corrected_answers, int wrong_answers, int opponentScore) {
        container.setLayout(null);
        wordLabel.setFont(new Font("Serif", Font.PLAIN, 18));
        wordLabel.setBounds(120, 20, 600, 150);

        int finalScore = corrected_answers * WQSettings.RIGHT_ANSWER_POINTS + wrong_answers * WQSettings.WRONG_ANSWER_POINTS;

        String finalSentence;

        if(isWinner == 1)
            finalSentence = "Congratulazioni, hai vinto! Hai guadagnato 3 punti extra, per un totale di " + (finalScore + 3)
                    + " punti.";
        else if(isWinner == -1) {
            opponentScore -= 3;
            finalSentence = "Purtroppo hai perso...";
        }
        else
            finalSentence = "Pareggio!";


        wordLabel.setText("<html><center>Hai tradotto correttamente " + corrected_answers + " parole, ne hai sbagliate "
                + wrong_answers + " e non risposto a " + (WQSettings.N_WORDS - (corrected_answers + wrong_answers))
                + ".<br/>Hai totalizzato " + finalScore + " punti.<br/>" + "Il tuo avversario ha totalizzato " +
                opponentScore + " punti.<br/><br/>" + finalSentence + "</center></html>");

        this.setTitle("WordQuizzle - Fine sfida");
        this.setBounds(10, 10, 800, 250);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        container.add(wordLabel);

        this.setModal(true);
        this.setVisible(true);
    }

}
