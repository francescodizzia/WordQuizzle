package com.dizzia.wordquizzle.GUI;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class GUI_Test {
    public JPanel panel1;
    private JButton button1;
    private JTextPane textPane1;
    private JTextField textField1;

    String s = "";

    public GUI_Test() {
        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
               // s = CMD_Handler.userpool.getRegisteredUsers();

                textPane1.setText(s);
            }
        });
    }

    public static void main(String[] args){
        JFrame frame = new JFrame("GUI_Test");
        frame.setContentPane(new GUI_Test().panel1);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
    }
}
