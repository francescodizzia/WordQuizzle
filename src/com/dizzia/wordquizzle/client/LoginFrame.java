package com.dizzia.wordquizzle.client;

import com.dizzia.wordquizzle.commons.RegisterInterface;
import com.dizzia.wordquizzle.commons.StatusCode;
import com.dizzia.wordquizzle.commons.WQSettings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class LoginFrame extends JFrame implements ActionListener{
    Container container = getContentPane();
    static Registry registry;
    static RegisterInterface stub;

    JTextField userTextField = new JTextField();
    JPasswordField passwordField = new JPasswordField();

    JLabel userLabel = new JLabel("Nome utente:");
    JLabel passwordLabel = new JLabel("Password:");

    JButton loginButton = new JButton("Login");
    JButton signInButton= new JButton("Registrati");
    JCheckBox showPassword = new JCheckBox("Mostra password");


    public LoginFrame(){
        container.setLayout(null);

        userLabel.setBounds(50,47,100,30);
        passwordLabel.setBounds(57,97,100,30);
        userTextField.setBounds(150,50,250,30);
        passwordField.setBounds(150,100,250,30);
        showPassword.setBounds(170,150,150,30);
        loginButton.setBounds(100,200,120,50);
        signInButton.setBounds(250,200,120,50);

        container.add(userLabel);
        container.add(passwordLabel);
        container.add(userTextField);
        container.add(passwordField);
        container.add(showPassword);
        container.add(loginButton);
        container.add(signInButton);

        loginButton.addActionListener(this);
        signInButton.addActionListener(this);
        showPassword.addActionListener(this);

        this.setTitle("WordQuizzle - Login");
        this.setVisible(true);
        this.setBounds(10, 10, 480, 340);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
    }



    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == signInButton) {
            int register_code = 0;
            String username = userTextField.getText();
            String password = String.valueOf(passwordField.getPassword());

            try {
                register_code = registra_utente(username, password);
            } catch (RemoteException | NotBoundException remoteException) {
                
                JOptionPane.showMessageDialog(this, "Errore: impossibile collegarsi al server",
                "Errore di rete", JOptionPane.ERROR_MESSAGE);
                remoteException.printStackTrace();

            }

            switch(register_code) {
                case StatusCode.OK:
                    int result = JOptionPane.showConfirmDialog(this, "Utente '"
                    + username + "' registrato con successo!\nPremere OK per effettuare il login.",
                            "Registrazione effettuata", JOptionPane.OK_CANCEL_OPTION);

                    if(result == JOptionPane.OK_OPTION)
                        WQClient.login(userTextField.getText(), String.valueOf(passwordField.getPassword()));

                    break;
                case StatusCode.EMPTY_PASSWORD:
                    JOptionPane.showMessageDialog(this, "Errore: la password inserita è vuota!",
                    "Errore di inserimento" , JOptionPane.ERROR_MESSAGE);
                    break;
                case StatusCode.USER_ALREADY_REGISTERED:
                    JOptionPane.showMessageDialog(this, "Errore: l'utente '" + username +
                            "risulta già registrato!", "Errore di registrazione" , JOptionPane.ERROR_MESSAGE);
                    break;
            }

        }

        if(e.getSource() == loginButton)
            WQClient.login(userTextField.getText(), String.valueOf(passwordField.getPassword()));

        if (e.getSource() == showPassword) {
            if (showPassword.isSelected())
                passwordField.setEchoChar((char) 0);
            else
                passwordField.setEchoChar('*');
        }
    }


    public static int registra_utente(String nickUtente, String password) throws RemoteException, NotBoundException {
        registry = LocateRegistry.getRegistry(WQSettings.RMI_IP, RegisterInterface.REG_PORT);
        stub = (RegisterInterface) registry.lookup(WQSettings.RMI_ADDRESS);
        return stub.registerUser(nickUtente, password);
    }

}
