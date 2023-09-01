package game.antman;

//package app.pacmanmattia;

import game.antman.Model;

import javax.swing.JFrame;

public class Antman extends JFrame{

    public Antman() {
        add(new Model());
    }


    public static void main(String[] args) {
       // app.pacmanmattia.Pacman pac = new app.pacmanmattia.Pacman();
        game.antman.Antman ant = new game.antman.Antman();
        ant.setVisible(true);
        ant.setTitle("Pacman");
        ant.setSize(380,420);
        ant.setDefaultCloseOperation(EXIT_ON_CLOSE);
        ant.setLocationRelativeTo(null);

    }

}