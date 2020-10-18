package com.dizzia.wordquizzle.client;

import javax.swing.*;
import java.awt.*;

public class ReportFrame extends JFrame {
    Container container = getContentPane();
    JLabel wordLabel = new JLabel();


    public ReportFrame(int isWinner, int corrected_answers, int wrong_answers){
        container.setLayout(null);

        wordLabel.setFont(new Font("Serif", Font.PLAIN, 18));
        wordLabel.setBounds(60,1, 600, 150);

        if(isWinner == 1) {
            wordLabel.setText("Hai vinto!");
        }else if(isWinner == -1){
            wordLabel.setText("Hai perso!");
        }else{
            wordLabel.setText("Pareggio!");
        }


        wordLabel.setText(wordLabel.getText() + "\nHai risposto correttamente a " + corrected_answers + " e ne sbagliate " + wrong_answers);

        this.setTitle("WordQuizzle - Fine sfida");
        this.setVisible(true);
        this.setBounds(10, 10, 480, 480);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        container.add(wordLabel);
    }

}
