package com.dizzia.wordquizzle.client;

import javax.swing.*;
import java.awt.*;

public class WaitingFrame extends JFrame {
    Container container = getContentPane();
    JLabel wordLabel = new JLabel();


    public WaitingFrame() {
        container.setLayout(null);

        wordLabel.setFont(new Font("Serif", Font.PLAIN, 18));
        wordLabel.setBounds(60,1, 400, 70);
        wordLabel.setText("In attesa che l'avversario concluda la partita...");

        this.setTitle("WordQuizzle - Attesa");
        this.setVisible(true);
        this.setBounds(10, 10, 480, 120);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        container.add(wordLabel);
    }


}


