package com.dizzia.wordquizzle.client;


import com.dizzia.wordquizzle.commons.StatusCode;
import com.dizzia.wordquizzle.commons.WQSettings;
import com.dizzia.wordquizzle.database.LeaderboardPair;
import com.google.gson.Gson;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class HubFrame extends JFrame implements ActionListener {
    JLabel lblNewLabel = new JLabel();
    JLabel scoreLabel = new JLabel();

    JButton addButton = new JButton();
    JButton friendlistButton = new JButton();
    JButton challengeButton = new JButton();
    JButton updateScoreButton = new JButton();
    JButton leaderscoreButton = new JButton();

    JButton logoutButton;

    DefaultListModel<String> listModel = new DefaultListModel<>();
    JList<String> list = new JList<>(listModel);

    JTextField textField = new JTextField();
    String username;

    Gson gson = new Gson();


    public HubFrame(String username){
        this.username = username;

        Container container = getContentPane();

        logoutButton = new JButton();
        logoutButton.setBounds(10, 341, 422, 53);
        logoutButton.setText("Logout");
        container.add(logoutButton);
        logoutButton.addActionListener(this);

        friendlistButton.setText("Aggiorna lista amici");
        friendlistButton.setBounds(10, 282, 422, 53);
        container.add(friendlistButton);
        friendlistButton.addActionListener(this);

        leaderscoreButton.setText("Mostra classifica");
        leaderscoreButton.setBounds(10, 223, 422, 53);
        container.add(leaderscoreButton);
        leaderscoreButton.addActionListener(this);

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


        lblNewLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblNewLabel.setBounds(10, 20, 422, 37);
        lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
        lblNewLabel.setText(username.toUpperCase());
        container.add(lblNewLabel);


        int score = getScore();
        scoreLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        scoreLabel.setBounds(10, 53, 422, 37);
        scoreLabel.setHorizontalAlignment(SwingConstants.CENTER);
        scoreLabel.setText("Punteggio: " + score);
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
                WQClient.writeString("challenge " + list.getSelectedValue());

                try {
                    ByteBuffer input = ByteBuffer.allocate(256);
                    input.clear();
                    WQClient.server.socket().setSoTimeout(WQSettings.CHALLENGE_REQUEST_TIMEOUT);

                    InputStream inStream = WQClient.server.socket().getInputStream();
                    ReadableByteChannel wrappedChannel = Channels.newChannel(inStream);
                    wrappedChannel.read(input);

                    input.flip();
                    String response = StandardCharsets.UTF_8.decode(input).toString();


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
            int result = WQClient.aggiungi_amico(textField.getText());
            if(result == StatusCode.OK)
                updateFriendList(WQClient.lista_amici());
            else
                JOptionPane.showMessageDialog(this, "Utente non trovato!",
                        "Errore", JOptionPane.ERROR_MESSAGE);
        }
        else if(e.getSource() == updateScoreButton){
            int score = getScore();
            scoreLabel.setText("Punteggio: " + score);
        }
        else if(e.getSource() == leaderscoreButton){
            String leaderboard = WQClient.classifica();
            LeaderboardPair[] friends = gson.fromJson(leaderboard, LeaderboardPair[].class);

            StringBuilder result = new StringBuilder();
            int i = 0;
            //TODO
            for(LeaderboardPair friend: friends)
                result.append("[").append(++i).append("#] ").append(friend.getUsername()).append(" (").append(friend.getScore()).append(")\n");

            JOptionPane.showMessageDialog(this, result,
                    "Classifica", JOptionPane.INFORMATION_MESSAGE);
        }
        else if (e.getSource() == logoutButton) {
            int choice = JOptionPane.showConfirmDialog(this,
                    "Sei davvero sicuro di voler effettuare il logout?", "Conferma di logout",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);

            if (choice == JOptionPane.YES_OPTION)
                try {
                    WQClient.server.close();
                    this.dispose();
                    WQClient.loginFrame = new LoginFrame();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
        }

    }


    private void updateFriendList(String jsonFriendlist) {
        String[] friends = gson.fromJson(jsonFriendlist, String[].class);

        listModel.clear();

        for(String friend: friends)
            listModel.addElement(friend);

        System.out.println(Arrays.toString(friends));
    }



    private int getScore(){
        WQClient.writeString("score");
        return WQClient.readInt();
    }


}
