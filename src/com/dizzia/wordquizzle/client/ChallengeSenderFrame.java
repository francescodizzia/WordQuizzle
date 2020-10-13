package com.dizzia.wordquizzle.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ChallengeSenderFrame extends JFrame implements ActionListener {
    Container container = getContentPane();

    JTextField challengeField = new JTextField();
    JButton challengeButton = new JButton("SFIDA");



    public ChallengeSenderFrame() {
        setLayoutManager();
        setLocationAndSize();
        addComponentsToContainer();
        addActionEvent();
    }

    public void setLocationAndSize() {

        challengeField.setBounds(150, 50, 250, 30);
        challengeButton.setBounds(250, 200, 120, 50);
    }

    public void addComponentsToContainer() {
        container.add(challengeField);
        container.add(challengeButton);
    }


    public void setLayoutManager() {
        container.setLayout(null);

    }


    public void addActionEvent() {
        challengeButton.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getSource() == challengeButton) {
            GUIClient.writeString("sfida " + challengeField.getText());
            System.out.println(GUIClient.readInt());
            System.out.println(GUIClient.readString());
            GUIClient.inizio_sfida();
        }


    }

}




