package com.dizzia.wordquizzle.client;


import com.google.gson.Gson;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

public class HubFrame extends JFrame implements ActionListener {
    JButton addButton = new JButton();
    JButton friendlistButton = new JButton();
    JButton challengeButton = new JButton();

    DefaultListModel<String> listModel = new DefaultListModel<>();
    JList<String> list = new JList<>(listModel);

    JTextField textField = new JTextField();
    String username;

    Gson gson = new Gson();


    public HubFrame(String username){
        this.username = username;

        Container container = getContentPane();

        JLabel lblNewLabel = new JLabel();
        lblNewLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblNewLabel.setAlignmentX(CENTER_ALIGNMENT);
        lblNewLabel.setBounds(10, 10, 422, 37);
        lblNewLabel.setText(username);
        container.add(lblNewLabel);

        JButton btnNewButton = new JButton();
        btnNewButton.setBounds(10, 341, 422, 53);
        btnNewButton.setText("Logout");
        container.add(btnNewButton);

        friendlistButton.setText("Aggiorna lista amici");
        friendlistButton.setBounds(10, 282, 422, 53);
        container.add(friendlistButton);
        friendlistButton.addActionListener(this);

        JButton btnMostraClassifica = new JButton();
        btnMostraClassifica.setText("Mostra classifica");
        btnMostraClassifica.setBounds(10, 223, 422, 53);
        container.add(btnMostraClassifica);

        JButton btnMostraPunteggio = new JButton();
        btnMostraPunteggio.setText("Mostra punteggio");
        btnMostraPunteggio.setBounds(10, 164, 422, 53);
        container.add(btnMostraPunteggio);


        challengeButton.setText("Gioca!");
        challengeButton.setBounds(10, 105, 422, 53);
        challengeButton.addActionListener(this);
        container.add(challengeButton);

        list.setBounds(438, 42, 334, 352);
        container.add(list);

        JLabel lblNewLabel_1 = new JLabel();
        lblNewLabel_1.setAlignmentX(CENTER_ALIGNMENT);
        lblNewLabel_1.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblNewLabel_1.setBounds(10, 53, 422, 46);
        lblNewLabel_1.setText("(+350)");
        container.add(lblNewLabel_1);

        textField.setBounds(438, 10, 275, 26);
        container.add(textField);

        addButton.setBounds(719, 10, 53, 26);
        addButton.setText("+");
        addButton.addActionListener(this);
        container.add(addButton);


        updateFriendList(WQClient.lista_amici());


        this.setTitle("[" + username + "] WordQuizzle - Hub di gioco");
        this.setSize(800, 453);
        this.setLayout(null);
        this.setBackground(null);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setLocationRelativeTo(null);
        this.setResizable(false);
        this.setVisible(true);

    }


    @Override
    public void actionPerformed(ActionEvent e) {

        if(e.getSource() == challengeButton) {
            WQClient.writeString("sfida " + list.getSelectedValue());

            String response = WQClient.readString();
            System.out.println(response);

            if(response.compareTo("TIMEOUT") == 0)
                JOptionPane.showMessageDialog(this, "Tempo scaduto, l'utente non ha risposto!",
                        "Errore", JOptionPane.ERROR_MESSAGE);
            else if(response.compareTo("REFUSED") == 0)
                JOptionPane.showMessageDialog(this, "L'utente ha rifiutato la sfida...",
                        "Errore", JOptionPane.ERROR_MESSAGE);
            else
                WQClient.inizio_sfida(response);
        }
        else if(e.getSource() == friendlistButton){
            updateFriendList(WQClient.lista_amici());

        }
        else if(e.getSource() == addButton){
            WQClient.aggiungi_amico(textField.getText());
            updateFriendList(WQClient.lista_amici());
        }


    }

    private void updateFriendList(String jsonFriendlist) {
        String[] friends = gson.fromJson(jsonFriendlist, String[].class);

        listModel.clear();

        for(String friend: friends){
            listModel.addElement(friend);
        }
        System.out.println(Arrays.toString(friends));
    }


}
