package com.dizzia.wordquizzle.client;


import com.dizzia.wordquizzle.commons.ByteBufferIO;
import com.dizzia.wordquizzle.commons.WQSettings;
import com.dizzia.wordquizzle.server.WQServer;
import com.google.gson.Gson;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HubFrame extends JFrame implements ActionListener {
    JLabel scoreLabel = new JLabel();
    JButton addButton = new JButton();
    JButton friendlistButton = new JButton();
    JButton challengeButton = new JButton();
    JButton updateScoreButton = new JButton();
    JButton logoutButton;

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

        logoutButton = new JButton();
        logoutButton.setBounds(10, 341, 422, 53);
        logoutButton.setText("Logout");
        container.add(logoutButton);
        logoutButton.addActionListener(this);

        friendlistButton.setText("Aggiorna lista amici");
        friendlistButton.setBounds(10, 282, 422, 53);
        container.add(friendlistButton);
        friendlistButton.addActionListener(this);

        JButton btnMostraClassifica = new JButton();
        btnMostraClassifica.setText("Mostra classifica");
        btnMostraClassifica.setBounds(10, 223, 422, 53);
        container.add(btnMostraClassifica);

        updateScoreButton.setText("Aggiorna punteggio");
        updateScoreButton.setBounds(10, 164, 422, 53);
        container.add(updateScoreButton);
        updateScoreButton.addActionListener(this);


        challengeButton.setText("Gioca!");
        challengeButton.setBounds(10, 105, 422, 53);
        challengeButton.addActionListener(this);
        container.add(challengeButton);

        list.setBounds(438, 42, 334, 352);
        container.add(list);

        int score = getScore();

        scoreLabel.setAlignmentX(CENTER_ALIGNMENT);
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        scoreLabel.setBounds(10, 53, 422, 46);
        scoreLabel.setText(String.valueOf(score));
        container.add(scoreLabel);

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
            if (list.getSelectedValue() != null) {
                WQClient.writeString("sfida " + list.getSelectedValue());


//                String response = WQClient.readString();
//                System.out.println(response);
                String response = "";

                try {
                    ByteBuffer input = ByteBuffer.allocate(256);
                    input.clear();
                    WQClient.server.socket().setSoTimeout(WQSettings.CHALLENGE_REQUEST_TIMEOUT);

                    InputStream inStream = WQClient.server.socket().getInputStream();
                    ReadableByteChannel wrappedChannel = Channels.newChannel(inStream);
                    wrappedChannel.read(input);
//                    WQClient.server.read(input);
                    input.flip();

                    response = StandardCharsets.UTF_8.decode(input).toString();


//                if (response.compareTo("TIMEOUT") == 0)
//                    JOptionPane.showMessageDialog(this, "Tempo scaduto, l'utente non ha risposto!",
//                            "Errore", JOptionPane.ERROR_MESSAGE);
//                else
                    if (response.compareTo("REFUSED") == 0)
                        JOptionPane.showMessageDialog(this, "L'utente ha rifiutato la sfida...",
                                "Errore", JOptionPane.ERROR_MESSAGE);
                    else
                        WQClient.inizio_sfida(response);

                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(this, "Tempo scaduto, l'utente non ha risposto!",
                            "Errore", JOptionPane.ERROR_MESSAGE);
                }


            }
            else
                JOptionPane.showMessageDialog(this, "Seleziona un amico da sfidare!\n" +
                                "Nel caso tu non abbia amici, aggiungili attraverso la barra e il pulsante in alto a destra!",
                        "Errore", JOptionPane.ERROR_MESSAGE);
        }
        else if (e.getSource() == friendlistButton)
            updateFriendList(WQClient.lista_amici());
        else if (e.getSource() == addButton) {
            WQClient.aggiungi_amico(textField.getText());
            updateFriendList(WQClient.lista_amici());
        }
        else if(e.getSource() == updateScoreButton){
            int score = getScore();
            scoreLabel.setText(String.valueOf(score));
        }
        else if (e.getSource() == logoutButton) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Sei davvero sicuro di voler effettuare il logout?", "Conferma di logout",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (choice == JOptionPane.YES_OPTION) {
                try {
                    WQClient.server.close();
                    this.dispose();
                    WQClient.loginFrame = new LoginFrame();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }

            }
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

    private int getScore(){
        WQClient.writeString("score");
        return WQClient.readInt();
    }


}
