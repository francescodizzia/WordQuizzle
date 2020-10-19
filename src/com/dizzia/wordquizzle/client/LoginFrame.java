package com.dizzia.wordquizzle.client;

import com.dizzia.wordquizzle.RegisterInterface;
import com.dizzia.wordquizzle.commons.StatusCode;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

public class LoginFrame extends JFrame implements ActionListener{
    Container container=getContentPane();

    JLabel userLabel = new JLabel("Nome utente:");
    JLabel passwordLabel = new JLabel("Password:");
    JTextField userTextField = new JTextField();
    JPasswordField passwordField = new JPasswordField();
    JButton loginButton = new JButton("Login");
    JButton signInButton= new JButton("Registrati");
    JCheckBox showPassword = new JCheckBox("Mostra password");

    static Registry registry;
    static RegisterInterface stub;

    public LoginFrame(){
        setLayoutManager();
        setLocationAndSize();
        addComponentsToContainer();
        addActionEvent();
    }

    public void setLocationAndSize()
    {

        userLabel.setBounds(50,47,100,30);
        passwordLabel.setBounds(57,97,100,30);

        userTextField.setBounds(150,50,250,30);
        passwordField.setBounds(150,100,250,30);
        showPassword.setBounds(170,150,150,30);

        loginButton.setBounds(100,200,120,50);
        signInButton.setBounds(250,200,120,50);
    }

    public void addComponentsToContainer()
    {
        //Adding each components to the Container
        container.add(userLabel);
        container.add(passwordLabel);
        container.add(userTextField);
        container.add(passwordField);
        container.add(showPassword);
        container.add(loginButton);
        container.add(signInButton);
    }


    public void setLayoutManager()
    {
        //Setting layout manager of Container to null
        container.setLayout(null);

    }


    public void addActionEvent() {
        loginButton.addActionListener(this);
        signInButton.addActionListener(this);
        showPassword.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {

        if (e.getSource() == signInButton) {
            String username = userTextField.getText();
            String password = String.valueOf(passwordField.getPassword());

            int register_code = 0;

            try {
                register_code = registra_utente(username, password);
            } catch (RemoteException | NotBoundException remoteException) {
                
                JOptionPane.showMessageDialog(this, "Errore: impossibile collegarsi al server",
                "Errore di rete", JOptionPane.ERROR_MESSAGE);
                
            }

            switch(register_code) {
                case StatusCode.OK:
                    int result = JOptionPane.showConfirmDialog(this, "Utente '"
                    + username + "' registrato con successo!\nPremere OK per effettuare il login.",
                            "Registrazione effettuata", JOptionPane.OK_CANCEL_OPTION);

                    if(result == JOptionPane.OK_OPTION){
                        ;
                    }

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

        if(e.getSource() == loginButton){
            WQClient.login(userTextField.getText(), String.valueOf(passwordField.getPassword()));
        }

        if (e.getSource() == showPassword) {
            if (showPassword.isSelected()) {
                passwordField.setEchoChar((char) 0);
            } else {
                passwordField.setEchoChar('*');
            }


        }

    }

    public static int registra_utente(String nickUtente, String password) throws RemoteException, NotBoundException {
        registry = LocateRegistry.getRegistry(RegisterInterface.REG_PORT);
        stub = (RegisterInterface) registry.lookup("WordQuizzle_" + RegisterInterface.MATRICOLA);
        return stub.registerUser(nickUtente, password);
    }




}
