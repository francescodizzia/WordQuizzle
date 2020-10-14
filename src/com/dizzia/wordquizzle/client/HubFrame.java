package com.dizzia.wordquizzle.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HubFrame extends JFrame implements ActionListener {
    Container container = getContentPane();

    JTextField challengeField = new JTextField();
    JButton challengeButton = new JButton("SFIDA");



    public HubFrame() {
        setLayoutManager();
        setLocationAndSize();
        addComponentsToContainer();
        addActionEvent();
    }

    public void setLocationAndSize() {
        challengeField.setBounds(65, 30, 300, 30);
        challengeButton.setBounds(140, 85, 150, 50);
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
            GUIClient.inizio_sfida();
        }


    }

}




