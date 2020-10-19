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
        this.firstWord = firstWord;
        container.setLayout(null);

        wordLabel.setFont(new Font("Serif", Font.BOLD, 30));
        wordLabel.setBounds(180,1, 150, 150);
        wordLabel.setText(firstWord);
        container.add(wordLabel);

        timerLabel.setFont(new Font("Serif", Font.BOLD, 16));
        timerLabel.setBounds(30,100, 150, 150);
        container.add(timerLabel);

        myWord.setBounds(100, 100, 250, 30);
        container.add(myWord);

        send.setBounds(150, 200, 120, 50);
        send.addActionListener(this);
        container.add(send);

        System.out.println(firstWord);

        this.setTitle("WordQuizzle - Sfida");
        this.setVisible(true);
        this.setBounds(10, 10, 480, 360);
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




