package com.example.exam;

import javax.swing.*;

/**
 * AntGame class extends JFrame and is used to start the game.
 * In main method a new AntGame is created, and then all details are added, such as title, size, default close operation etc..
 */
public class AntGame extends JFrame {

    /**
     * Construct a new AntGame, adding a Model class
     */
    public AntGame() {
        add(new Model());
    }


    public static void main(String[] args) {
        AntGame ant = new AntGame();
        ant.setVisible(true);
        ant.setIconImage(new ImageIcon("src/main/resources/icon.png").getImage());
        ant.setTitle("AntGame");
        ant.setResizable(false);
        ant.setSize(380,420);
        ant.setDefaultCloseOperation(EXIT_ON_CLOSE);
        ant.setLocationRelativeTo(null);
    }
}
