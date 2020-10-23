package com.dizzia.wordquizzle.client;

import javax.swing.*;
import java.awt.*;

public class WaitingDialog extends JDialog {
    Container container = getContentPane();
    JLabel wordLabel = new JLabel();


    public WaitingDialog() {
        container.setLayout(null);

        wordLabel.setFont(new Font("Serif", Font.PLAIN, 18));
        wordLabel.setBounds(60,1, 400, 70);
        wordLabel.setText("In attesa che l'avversario concluda la partita...");

        this.setTitle("WordQuizzle - Attesa");
        this.setBounds(10, 10, 480, 120);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        container.add(wordLabel);
        this.setVisible(true);
    }


}


