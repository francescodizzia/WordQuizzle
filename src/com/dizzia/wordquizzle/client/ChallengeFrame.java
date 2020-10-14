package com.dizzia.wordquizzle.client;

import com.dizzia.wordquizzle.server.ChallengeHandler;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChallengeFrame extends JFrame implements ActionListener {
    Container container = getContentPane();

    JTextField myWord = new JTextField();
    JButton send = new JButton("INVIA");
    JLabel wordLabel = new JLabel("null");



    public ChallengeFrame() {
        setLayoutManager();
        setLocationAndSize();
        addComponentsToContainer();
        addActionEvent();
    }

    public void setLocationAndSize() {

        wordLabel.setFont(new Font("Serif", Font.BOLD, 30));
        wordLabel.setBounds(180,1, 150, 150);
        myWord.setBounds(100, 100, 250, 30);
        send.setBounds(150, 200, 120, 50);


        String firstWord = GUIClient.readString();
        wordLabel.setText(firstWord);
        System.out.println(firstWord);

        this.setTitle("WordQuizzle - Sfida");
        this.setVisible(true);
        this.setBounds(10, 10, 480, 360);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);

    }

    public void addComponentsToContainer() {
        container.add(myWord);
        container.add(send);
        container.add(wordLabel);
    }


    public void setLayoutManager() {
        container.setLayout(null);

    }


    public void addActionEvent() {
        send.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getSource() == send) {
            GUIClient.writeString(myWord.getText().toLowerCase());
            String k = GUIClient.readString();
            System.out.println(k);
            wordLabel.setText(k);
        }


    }


}




