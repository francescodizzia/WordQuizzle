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
    JLabel wordCounter = new JLabel();
    TimerLabel timerLabel = new TimerLabel(this);
    String firstWord;
    int clicked = 0;



    public ChallengeFrame(String firstWord) {
        ImageIcon icon = new ImageIcon("./resources/it.jpg");
        JLabel label = new JLabel();
        label.setBounds(130, 90, 150, 100);
        label.setIcon(icon);
        container.add(label);

        ImageIcon icon2 = new ImageIcon("./resources/uk.jpg");
        JLabel label2 = new JLabel();
        label2.setBounds(500, 90, 150, 100);
        label2.setIcon(icon2);
        container.add(label2);

        this.firstWord = firstWord;
        container.setLayout(null);

        wordCounter.setFont(new Font("Serif", Font.BOLD, 24));
        wordCounter.setBounds(700,15, 100, 50);
        wordCounter.setText("1/" + WQSettings.N_WORDS);
        container.add(wordCounter);


        wordLabel.setFont(new Font("Serif", Font.BOLD, 16));
        wordLabel.setBounds(125,275, 140, 30);
        wordLabel.setHorizontalAlignment(SwingConstants.CENTER);
        wordLabel.setText(firstWord.toUpperCase());
        container.add(wordLabel);


        timerLabel.setFont(new Font("Serif", Font.BOLD, 24));
        timerLabel.setBounds(360,160, 150, 150);
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
            if (myWord.getText() != null && myWord.getText().compareTo("") != 0) {
                WQClient.writeString(myWord.getText().toLowerCase());
                myWord.setText("");
                clicked++;

                if (clicked == WQSettings.N_WORDS) {
                    this.dispose();
                    WaitingDialog waitingDialog = new WaitingDialog();
                    WQClient.waitEnd(waitingDialog);
                } else {
                    String k = WQClient.readString();

                    String[] results = k.split(" ");
                    if(results[0].compareTo("FIN") == 0){
                        this.dispose();
                        WaitingDialog waitingDialog = new WaitingDialog();

                        int winner = Integer.parseInt(results[1]);
                        int correct_answers = Integer.parseInt(results[2]);
                        int wrong_answers = Integer.parseInt(results[3]);
                        int opponentScore = Integer.parseInt(results[4]);

                        waitingDialog.dispose();

                        new ReportDialog(winner, correct_answers, wrong_answers, opponentScore);
                        WQClient.hubFrame.setVisible(true);
                    }

                    System.out.println(k);
                    wordCounter.setText((clicked+1) + "/" + WQSettings.N_WORDS);
                    wordLabel.setText(k.toUpperCase());
                }
            }
        }

    }

}




