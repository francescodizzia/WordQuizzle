package com.dizzia.wordquizzle.client;

import com.dizzia.wordquizzle.commons.WQSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChallengeFrame extends JFrame implements ActionListener {
    Container container = getContentPane();

    JTextField myWord = new JTextField();
    JButton send = new JButton("INVIA");
    JLabel wordLabel = new JLabel("null");
    TimerLabel timerLabel = new TimerLabel(this);

    String firstWord;

    int clicked = 0;



    public ChallengeFrame(String firstWord) {
        ImageIcon icon = new ImageIcon("./italy.jpg");
        JLabel label = new JLabel();
        label.setBounds(130, 90, 150, 100);
        label.setIcon(icon);
        container.add(label);

        ImageIcon icon2 = new ImageIcon("./uk.jpg");
        JLabel label2 = new JLabel();
        label2.setBounds(500, 90, 150, 100);
        label2.setIcon(icon2);
        container.add(label2);

        this.firstWord = firstWord;
        container.setLayout(null);

        wordLabel.setFont(new Font("Serif", Font.BOLD, 16));
        wordLabel.setBounds(165,280, 150, 30);
        wordLabel.setText(firstWord);
        container.add(wordLabel);

        timerLabel.setFont(new Font("Serif", Font.BOLD, 16));
        timerLabel.setBounds(30,100, 150, 150);
        container.add(timerLabel);

        myWord.setBounds(500, 277, 144, 26);
        container.add(myWord);

        send.setBounds(315, 340, 130, 70);
        send.addActionListener(this);
        container.add(send);

        System.out.println(firstWord);

        this.setTitle("WordQuizzle - Sfida");
        this.setVisible(true);
        this.setBounds(10, 10, 800, 500);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
    }



    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getSource() == send) {
            WQClient.writeString(myWord.getText().toLowerCase());
            myWord.setText("");
            clicked++;

            if(clicked == WQSettings.N_WORDS){
                this.setVisible(false);
                WaitingDialog waitingDialog = new WaitingDialog();
                this.dispose();
                WQClient.waitEnd(waitingDialog);
            }
            else {
                String k = WQClient.readString();
                System.out.println(k);
                wordLabel.setText(k);
            }
        }


    }


}




