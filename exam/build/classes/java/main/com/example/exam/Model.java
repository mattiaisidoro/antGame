package com.example.exam;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;

/**
 * Model class extends JPanel implements ActionListener.
 * This class creates the entire game: variables declarations, variables settings, sprites painting and game's mechanics
 */

public class Model extends JPanel implements ActionListener {

    /*-------------------------------- variables declaration--------------------------------*/
    private Dimension d;
    private final Font smallFont = new Font("Arial", Font.BOLD, 14);
    private boolean inGame = false;
    private boolean dying = false;

    private boolean dead = false;

    private final int BLOCK_SIZE = 24;
    private final int N_BLOCKS = 15;
    private final int SCREEN_SIZE = N_BLOCKS * BLOCK_SIZE;

    private int N_SNAKE;
    private int lives, score;
    private int[] dx, dy;
    private int[] snake_x, snake_y, snake_dx, snake_dy, snakeSpeed;

    private Image heart;
    private Image up, down, left, right;
    private Image snake_up, snake_down, snake_left, snake_right;
    private final Clip[] music = new Clip[3];

    private int ant_x, ant_y, ant_dx, ant_dy;
    private int req_dx, req_dy;

    private final short[] levelData = {
            19, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 18, 22,
            17, 16, 16, 16, 16, 24, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            25, 24, 24, 24, 28, 0, 17, 16, 16, 16, 16, 16, 16, 16, 20,
            0,  0,  0,  0,  0,  0, 17, 16, 16, 16, 16, 16, 16, 16, 20,
            19, 18, 18, 18, 18, 18, 16, 16, 16, 16, 24, 24, 24, 24, 20,
            17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
            17, 16, 16, 16, 16, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
            17, 16, 16, 16, 24, 16, 16, 16, 16, 20, 0,  0,  0,   0, 21,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 18, 18, 18, 18, 20,
            17, 24, 24, 28, 0, 25, 24, 24, 16, 16, 16, 16, 16, 16, 20,
            21, 0,  0,  0,  0,  0,  0,   0, 17, 16, 16, 16, 16, 16, 20,
            17, 18, 18, 22, 0, 19, 18, 18, 16, 16, 16, 16, 16, 16, 20,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            17, 16, 16, 20, 0, 17, 16, 16, 16, 16, 16, 16, 16, 16, 20,
            25, 24, 24, 24, 26, 24, 24, 24, 24, 24, 24, 24, 24, 24, 28
    };

    /*
    level data explanation: all the number are results of sums of number set as default, so each number inside the array
    represents a specific part of the map.
    Number explanation:
    0 -> solid block, sprites can't go through it;
    1-> left block, it means that you can't turn left;
    2-> upper block, it means that you can't go straight;
    4-> right block, it means that you can't turn right;
    8-> lower block, it means that you can't go down;
    16-> white ball;
     */
    private final int[] validSpeeds = {1, 2, 3, 4, 6, 8};

    private int currentSpeed = 3;
    private short[] screenData;
    private Timer timer;

    /**
     * Construct a new Model.
     * It invokes 6 methods used to:
     * - Set clip files;
     * - Load images;
     * - Initialize variables
     * - Set a KeyListener
     * - Make the component focusable;
     * - Initialize game
     */
    public Model() {

        setClip();
        loadImages();
        initVariables();
        addKeyListener(new Model.TAdapter());
        setFocusable(true);
        initGame();
    }

    /**
     * Method used to initialize the Clip array, loading Audio files in it.
     * ATTENTION: it may throw exceptions if audio files aren't load correctly, or if the path name passed isn't correct
     */
    private void setClip(){
        try {
            AudioInputStream ais1 = AudioSystem.getAudioInputStream(new File("src/main/resources/antgame.wav"));
            AudioInputStream ais2 = AudioSystem.getAudioInputStream(new File("src/main/resources/fanfare.wav"));
            AudioInputStream ais3 = AudioSystem.getAudioInputStream(new File("src/main/resources/gameover.wav"));
            for (int i = 0; i < 3; i++){
                music[i] = AudioSystem.getClip();
            }
            music[0].open(ais1);
            music[1].open(ais2);
            music[2].open(ais3);
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Method used to load images in their specific variable.
     * ATTENTION: in this case there is no worry about exceptions,
     * indeed the game will start also if the path name of the file isn't load correctly
     * (in this case there won't be any image, but the game will run anyway).
     */
    private void loadImages() {
        down = new ImageIcon("src/main/resources/down_ant.gif").getImage();
        up = new ImageIcon("src/main/resources/up_ant.gif").getImage();
        left = new ImageIcon("src/main/resources/left_ant.gif").getImage();
        right = new ImageIcon("src/main/resources/right_ant.gif").getImage();
        snake_up = new ImageIcon("src/main/resources/snake_up.gif").getImage();
        snake_down = new ImageIcon("src/main/resources/snake_down.gif").getImage();
        snake_left = new ImageIcon("src/main/resources/snake_left.gif").getImage();
        snake_right = new ImageIcon("src/main/resources/snake_right.gif").getImage();
        heart = new ImageIcon("src/main/resources/heart.png").getImage();
    }

    /**
     * Method used to initialize variables, such as screen_data, the dimension of game panel and others.
     * It also starts the timer, so actions are listened to model class.
     */
    private void initVariables() {

        screenData = new short[N_BLOCKS * N_BLOCKS];
        d = new Dimension(400, 400);
        int MAX_GHOSTS = 6;
        snake_x = new int[MAX_GHOSTS];
        snake_dx = new int[MAX_GHOSTS];
        snake_y = new int[MAX_GHOSTS];
        snake_dy = new int[MAX_GHOSTS];
        snakeSpeed = new int[MAX_GHOSTS];
        dx = new int[4];
        dy = new int[4];

        timer = new Timer(60, this);
        timer.start();
    }

    /**
     *This method is called as intermediate method: if the ant is dead, it calls the death method,
     *  otherwise it calls method to move sprites
     * @param g2d the graphic where sprites are drawn
     */

    private void playGame(Graphics2D g2d) {

        if (dying) {

            death();

        } else {

            moveAnt();
            drawAnt(g2d);
            moveSnakes(g2d);
            checkMaze();
        }
    }

    /**
     * Method called to draw the Intro Screen Sentence
     * @param g2d graphic where the sentence will be drawn
     */

    private void showIntroScreen(Graphics2D g2d) {

        String start = "Press SPACE to start";
        g2d.setColor(Color.yellow);
        g2d.drawString(start, (SCREEN_SIZE)/4, 150);
    }

    /**
     * Method called to draw the Outro Screen Sentence, depending on the level the player reached.
     * It also starts a clip, as before, depending on the level reached.
     * @param g2d graphic where the sentence will be drawn
     */
    private void showOutroScreen(Graphics2D g2d) {
        music[0].stop();

        int lev = (score / 194) + 1;
        if(lev < 7)
        {
            String s = ("YOU REACHED LEVEL: " + lev + " ON 6");
            g2d.setColor(Color.white);
            g2d.drawString(s, 12, 150);
            String start = "Press SPACE to restart";
            g2d.setColor(Color.yellow);
            g2d.drawString(start, 12, 173);
            music[2].start();
        }
        else if (lev == 7){
            String s = "WELL DONE YOU COMPLETED THE GAME!";
            g2d.setColor(Color.white);
            g2d.drawString(s, 12, 150);
            String start = "Press SPACE to start again";
            g2d.setColor(Color.yellow);
            g2d.drawString(start, 12, 173);
            music[1].setMicrosecondPosition(0);
            music[1].start();
        }
    }

    /**
     * Method called to draw score the player reached, it increases when a white dot is taken.
     * @param g the graphic where the score will be drawn
     */
    private void drawScore(Graphics2D g) {
        g.setFont(smallFont);
        g.setColor(new Color(5, 181, 79));
        String s = "Score: " + score;
        g.drawString(s, SCREEN_SIZE / 2 + 96, SCREEN_SIZE + 16);
        int lev = (score / 194) +1 ;
        String l = "level: " + lev;
        g.drawString(l, SCREEN_SIZE / 2 + 25 , SCREEN_SIZE + 16);

        for (int i = 0; i < lives; i++) {
            g.drawImage(heart, i * 28 + 8, SCREEN_SIZE + 1, this);
        }
    }

    /**
     *The method checks maze and game state, if the player finished the current level, the next one is initialized,
     * until the final level is reached. Instead, if the current level isn't finished yet, initLevel method isn't invoked.
     */
    private void checkMaze() {
        boolean finished = true;
        int lev = (score /194)+1 ;
        if (score % 194 != 0){
            finished = false;
        }

        if (finished && lev != 7) {

            N_SNAKE++;
            int maxSpeed = 3;
            if (currentSpeed < maxSpeed) {
                currentSpeed++;
            }
            if(lives != 3)
            {
                lives=3;
            }

            initLevel();
        }
        else if(lev==7)
        {
            inGame=false;
        }
    }

    /**
     * The method is invoked when the player lose a life, depending on left number of lives, the game continues or stops
     */

    private void death() {

        lives--;

        if (lives == 0) {
            inGame = false;
            dead = true;
        }

        continueLevel();
    }

    /**
     * Method called to move snakes. It checks snakes movement direction, and it changes if there is a solid block.
     * It also generates randomly snakes direction and checks if the ant dies touching a snake.
     * The method invokes the drawSnakes function to draw sprites on game panel.
     * @param g2d the graphic where snakes will be drawn
     */
    private void moveSnakes(Graphics2D g2d) {

        int pos;
        int count;

        for (int i = 0; i < N_SNAKE; i++) {
            if (snake_x[i] % BLOCK_SIZE == 0 && snake_y[i] % BLOCK_SIZE == 0) {
                pos = snake_x[i] / BLOCK_SIZE + N_BLOCKS * (snake_y[i] / BLOCK_SIZE);

                count = 0;

                if ((screenData[pos] & 1) == 0 && snake_dx[i] != 1) {
                    dx[count] = -1;
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 2) == 0 && snake_dy[i] != 1) {
                    dx[count] = 0;
                    dy[count] = -1;
                    count++;
                }

                if ((screenData[pos] & 4) == 0 && snake_dx[i] != -1) {
                    dx[count] = 1;
                    dy[count] = 0;
                    count++;
                }

                if ((screenData[pos] & 8) == 0 && snake_dy[i] != -1) {
                    dx[count] = 0;
                    dy[count] = 1;
                    count++;
                }

                count = (int) (Math.random() * count);

                if (count > 3) {
                    count = 3;
                }

                snake_dx[i] = dx[count];
                snake_dy[i] = dy[count];

            }

            snake_x[i] = snake_x[i] + (snake_dx[i] * snakeSpeed[i]);
            snake_y[i] = snake_y[i] + (snake_dy[i] * snakeSpeed[i]);
            drawSnakes(g2d, snake_x[i] + 1, snake_y[i] + 1, snake_dx[i], snake_dy[i]);

            if (ant_x > (snake_x[i] - 12) && ant_x < (snake_x[i] + 12)
                    && ant_y > (snake_y[i] - 12) && ant_y < (snake_y[i] + 12)
                    && inGame) {

                dying = true;
            }
        }
    }

    /**
     * The method draw snakes sprites on game board, depending on their direction
     * @param g2d the graphic where sprites will be drawn;
     * @param x is used to set the x sprite location on the board
     * @param y is used to set the y sprite location on the board
     * @param dx is used to get the direction of the snake on x
     * @param dy is used to get the direction of the snake on y
     */
    private void drawSnakes(Graphics2D g2d, int x, int y, int dx, int dy) {
        if (dx < 0) {
            g2d.drawImage(snake_left, x, y, this);
        } else if (dx > 0) {
            g2d.drawImage(snake_right, x,y, this);
        } else if (dy < 0) {
            g2d.drawImage(snake_up, x, y, this);
        } else {
            g2d.drawImage(snake_down, x, y, this);
        }
    }

    /**
     * The method moves ant sprite on the board, depending on detected direction.
     * It also checks if the sprite can move in that direction, and it removes dots if the ant takes them.
     * Ant position is updated too.
     */
    private void moveAnt() {

        int pos;
        short ch;

        if (ant_x % BLOCK_SIZE == 0 && ant_y % BLOCK_SIZE == 0) {
            pos = ant_x / BLOCK_SIZE + N_BLOCKS * (ant_y / BLOCK_SIZE);
            ch = screenData[pos];

            if ((ch & 16) != 0) {
                screenData[pos] = (short) (ch & 15);
                score++;
            }

            if (req_dx != 0 || req_dy != 0) {
                if (!((req_dx == -1 && req_dy == 0 && (ch & 1) != 0)
                        || (req_dx == 1 && req_dy == 0 && (ch & 4) != 0)
                        || (req_dx == 0 && req_dy == -1 && (ch & 2) != 0)
                        || (req_dx == 0 && req_dy == 1 && (ch & 8) != 0))) {
                    ant_dx = req_dx;
                    ant_dy = req_dy;
                }
            }


            if ((ant_dx == -1 && ant_dy == 0 && (ch & 1) != 0)
                    || (ant_dx == 1 && ant_dy == 0 && (ch & 4) != 0)
                    || (ant_dx == 0 && ant_dy == -1 && (ch & 2) != 0)
                    || (ant_dx == 0 && ant_dy == 1 && (ch & 8) != 0)) {
                ant_dx = 0;
                ant_dy = 0;
            }
        }
        int ANT_SPEED = 4;
        ant_x = ant_x + ANT_SPEED * ant_dx;
        ant_y = ant_y + ANT_SPEED * ant_dy;
    }

    /**
     * Method called to draw ant sprite on game panel, depending on its direction.
     * NOTE: in this case ant_x, ant_y aren't parameter of the method like in drawSnakes.
     * @param g2d the graphic where sprites will be drawn
     */
    private void drawAnt(Graphics2D g2d) {

        if (req_dx == -1) {
            g2d.drawImage(left, ant_x + 1, ant_y + 1, this);
        } else if (req_dx == 1) {
            g2d.drawImage(right, ant_x + 1, ant_y + 1, this);
        } else if (req_dy == -1) {
            g2d.drawImage(up, ant_x + 1, ant_y + 1, this);
        } else {
            g2d.drawImage(down, ant_x + 1, ant_y + 1, this);
        }
    }

    /**
     * The method draw the maze and update level data, to see if a dot has to be drawn or not.
     * @param g2d the graphic where sprites will be drawn.
     */

    private void drawMaze(Graphics2D g2d) {

        short i = 0;
        int x, y;

        for (y = 0; y < SCREEN_SIZE; y += BLOCK_SIZE) {
            for (x = 0; x < SCREEN_SIZE; x += BLOCK_SIZE) {

                g2d.setColor(new Color(20,100,150));
                g2d.setStroke(new BasicStroke(5));

                if ((levelData[i] == 0)) {
                    g2d.fillRect(x, y, BLOCK_SIZE, BLOCK_SIZE);
                }


                if ((screenData[i] & 1) != 0) {
                    g2d.drawLine(x, y, x, y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 2) != 0) {
                    g2d.drawLine(x, y, x + BLOCK_SIZE - 1, y);
                }

                if ((screenData[i] & 4) != 0) {
                    g2d.drawLine(x + BLOCK_SIZE - 1, y, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 8) != 0) {
                    g2d.drawLine(x, y + BLOCK_SIZE - 1, x + BLOCK_SIZE - 1,
                            y + BLOCK_SIZE - 1);
                }

                if ((screenData[i] & 16) != 0) {
                    g2d.setColor(new Color(255,255, 255));
                    g2d.fillOval(x + 10, y + 10, 6, 6);
                }

                i++;
            }
        }
    }

    /**
     * Method called to initialize the game (settings of variables that must be the same at the beginning of every game).
     * NOTE: music's elements are put there to reset them avery game, also if they haven't been played yet.
     */
    private void initGame() {

        lives = 3;
        score = 0;
        initLevel();
        N_SNAKE = 1;
        currentSpeed = 2;
        music[1].setMicrosecondPosition(0);
        music[2].setMicrosecondPosition(0);
    }

    /**
     * Method called to set screen data array and then invoke continue level function
     */
    private void initLevel() {

        int i;
        for (i = 0; i < N_BLOCKS * N_BLOCKS; i++) {
            screenData[i] = levelData[i];
        }

        continueLevel();
    }

    /**
     * Method called to set initial position of sprites (snakes and ant), it also set the initial direction and speed of snakes
     * and resets both ant direction move and control
     */
    private void continueLevel() {

        int dx = 1;
        int random;

        for (int i = 0; i < N_SNAKE; i++) {

            snake_y[i] = 4 * BLOCK_SIZE; //start position
            snake_x[i] = 4 * BLOCK_SIZE;
            snake_dy[i] = 0;
            snake_dx[i] = dx;
            dx = -dx;
            random = (int) (Math.random() * (currentSpeed + 1));

            if (random > currentSpeed) {
                random = currentSpeed;
            }

            snakeSpeed[i] = validSpeeds[random];
        }

        ant_x = 7 * BLOCK_SIZE;
        ant_y = 11 * BLOCK_SIZE;
        ant_dx = 0;
        ant_dy = 0;
        req_dx = 0;
        req_dy = 0;
        dying = false;
    }


    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        int lev = (score /194) +1;
        Graphics2D g2d = (Graphics2D) g;
        g2d.setColor(new Color(102, 51, 0));
        g2d.fillRect(0, 0, d.width, d.height);

        drawMaze(g2d);
        drawScore(g2d);

        if (inGame){
            playGame(g2d);
        }
        else {

            if (!dead && lev < 7)
                showIntroScreen(g2d);
            else{
                showOutroScreen(g2d);
            }
        }
        Toolkit.getDefaultToolkit().sync();
        g2d.dispose();
    }


    /**
     * TAdapter class is created to extend KeyAdapter, so it's used to listen key events, in particular when a key is pressed.
     * It uses key: up, left, right and down for movements, escape key to stop the game and restarting it,
     * instead space key is used to start a game.
     * NOTE: When space key is pressed main music starts, while the others stop, also if they weren't playing.
     */
    class TAdapter extends KeyAdapter {

        @Override
        public void keyPressed(KeyEvent e) {

            int key = e.getKeyCode();

            if (inGame) {
                if (key == KeyEvent.VK_LEFT) {
                    req_dx = -1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_RIGHT) {
                    req_dx = 1;
                    req_dy = 0;
                } else if (key == KeyEvent.VK_UP) {
                    req_dx = 0;
                    req_dy = -1;
                } else if (key == KeyEvent.VK_DOWN) {
                    req_dx = 0;
                    req_dy = 1;
                } else if (key == KeyEvent.VK_ESCAPE && timer.isRunning()) {
                    music[0].stop();
                    inGame = false;
                }
            } else {
                if (key == KeyEvent.VK_SPACE) {
                    inGame = true;
                    initGame();
                    music[1].stop();
                    music[2].stop();
                    music[0].setMicrosecondPosition(0);
                    music[0].start();
                    music[0].loop(music[0].LOOP_CONTINUOUSLY);
                }
            }
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
}
