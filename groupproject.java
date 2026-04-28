import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class groupproject {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Super Colonzo World");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        ObbyGame game = new ObbyGame();
        frame.add(game);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        game.start(); // Kick off the game loop
    }
}

class Platform {
    int x, y, w, h;
    Color color;
    boolean isGoal; // Replaced isLava logic with isGoal for clarity

    public Platform(int x, int y, int w, int h, Color color, boolean isGoal) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.color = color;
        this.isGoal = isGoal;
    }
}

class ObbyGame extends JPanel implements Runnable {
    private Thread gameThread;
    
    // --- GAME STATES ---
    private final int MENU = 0;
    private final int PLAYING = 1;
    private final int SETTINGS = 2;
    private final int GAMEOVER = 3;
    private int gameState = MENU;

    // --- MARIO THEME COLORS ---
    private final Color MARIO_SKY = new Color(107, 140, 255);
    private final Color MARIO_GRASS = new Color(0, 168, 0);
    private final Color MARIO_BRICK = new Color(200, 76, 12);
    private final Color MARIO_PIPE = new Color(40, 180, 40);
    private final Color DIRT_BROWN = new Color(130, 80, 40);
    private final Color Q_BLOCK = new Color(255, 160, 0);

    // LEVEL TRACKING ADDED
    private int currentLevel = 1;
    private final int MAX_LEVEL = 5;

    // MOVED THE PLAYER VARIABLES HERE
    private int playerX = 100, playerY = 100;
    private double velY = 0;
    private final double GRAVITY = 0.5;
    
    // Added these for movement
    private boolean left = false, right = false;
    private boolean jumpPressed = false; 
    private boolean jumping = false;
    private boolean facingRight = true; 

    // LIVES SYSTEM ADDED (Removed Death Tracker)
    private int lives = 3;

    // CAMERA VARIABLE ADDED
    private int cameraX = 0;

    // Add to ObbyGame: 
    java.util.List<Platform> platforms = new java.util.ArrayList<>();

    public ObbyGame() {
        this.setPreferredSize(new Dimension(800, 600));
        this.setBackground(MARIO_SKY);
        this.setFocusable(true);

        // --- Define Level Platforms ---
        loadLevel(currentLevel);
        
        this.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (gameState == MENU) {
                    if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                        lives = 3; // Reset lives on new game
                        currentLevel = 1;
                        loadLevel(currentLevel);
                        gameState = PLAYING;
                    }
                    if(e.getKeyCode() == KeyEvent.VK_S) gameState = SETTINGS;
                } else if (gameState == GAMEOVER) {
                    if(e.getKeyCode() == KeyEvent.VK_ENTER) gameState = MENU;
                } else if (gameState == SETTINGS) {
                    if(e.getKeyCode() == KeyEvent.VK_ESCAPE) gameState = MENU;
                } else if (gameState == PLAYING) {
                    if(e.getKeyCode() == KeyEvent.VK_A) left = true;
                    if(e.getKeyCode() == KeyEvent.VK_D) right = true;
                    if(e.getKeyCode() == KeyEvent.VK_SPACE) jumpPressed = true;
                    if(e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        gameState = MENU;
                        resetPlayer();
                    }
                }
            }
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_A) left = false;
                if(e.getKeyCode() == KeyEvent.VK_D) right = false;
                if(e.getKeyCode() == KeyEvent.VK_SPACE) jumpPressed = false;
            }
        });
    }

    private void loadLevel(int level) {
        platforms.clear();
        resetPlayer(); 
        
        switch(level) {
            case 1:
                platforms.add(new Platform(0, 500, 500, 100, MARIO_GRASS, false));
                platforms.add(new Platform(300, 440, 80, 60, MARIO_GRASS, false));
                platforms.add(new Platform(550, 400, 120, 40, MARIO_BRICK, false));
                platforms.add(new Platform(800, 340, 60, 260, MARIO_PIPE, true)); 
                break;
            case 2:
                platforms.add(new Platform(0, 500, 200, 100, MARIO_GRASS, false));
                platforms.add(new Platform(300, 360, 40, 40, Q_BLOCK, false));
                platforms.add(new Platform(340, 360, 40, 40, MARIO_BRICK, false));
                platforms.add(new Platform(550, 460, 240, 140, MARIO_GRASS, false));
                platforms.add(new Platform(900, 340, 60, 260, MARIO_PIPE, true));
                break;
            case 3:
                platforms.add(new Platform(0, 500, 150, 100, MARIO_GRASS, false));
                platforms.add(new Platform(300, 380, 40, 40, MARIO_BRICK, false));
                platforms.add(new Platform(450, 300, 40, 40, Q_BLOCK, false));
                platforms.add(new Platform(600, 220, 40, 40, MARIO_BRICK, false));
                platforms.add(new Platform(850, 340, 60, 260, MARIO_PIPE, true));
                break;
            case 4:
                platforms.add(new Platform(0, 500, 250, 100, MARIO_GRASS, false));
                platforms.add(new Platform(400, 420, 100, 40, MARIO_BRICK, false));
                platforms.add(new Platform(650, 320, 100, 40, MARIO_BRICK, false));
                platforms.add(new Platform(900, 240, 60, 360, MARIO_PIPE, true));
                break;
            case 5:
                platforms.add(new Platform(0, 500, 80, 100, MARIO_GRASS, false));
                platforms.add(new Platform(200, 400, 40, 40, Q_BLOCK, false));
                platforms.add(new Platform(400, 320, 80, 40, MARIO_BRICK, false));
                platforms.add(new Platform(600, 240, 40, 40, Q_BLOCK, false));
                platforms.add(new Platform(850, 340, 60, 260, MARIO_PIPE, true));
                break;
        }
    }

    public void start() {
        if (gameThread == null) {
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    private void updatePhysics() {
        if (gameState != PLAYING) return;

        if (left) { playerX -= 6; facingRight = false; }
        if (right) { playerX += 6; facingRight = true; }

        velY += GRAVITY;
        playerY += velY;

        if (jumpPressed && !jumping) {
            velY = -12;
            jumping = true;
        }

        Rectangle playerBounds = new Rectangle(playerX, (int)playerY, 32, 32);
        for (Platform p : platforms) {
            Rectangle platBounds = new Rectangle(p.x, p.y, p.w, p.h);
            if (playerBounds.intersects(platBounds)) {
                if (velY > 0) {
                    velY = 0;
                    playerY = p.y - 32;
                    jumping = false;
                    if (p.isGoal) {
                        if (currentLevel < MAX_LEVEL) {
                            currentLevel++;
                            loadLevel(currentLevel);
                        } else {
                            JOptionPane.showMessageDialog(this, "Victory!");
                            gameState = MENU;
                        }
                        return;
                    }
                }
            }
        }

        // --- LIVES SYSTEM LOGIC ---
        if (playerY > 600) {
            lives--;
            if (lives > 0) {
                resetPlayer();
            } else {
                gameState = GAMEOVER;
            }
        }

        cameraX = playerX - 300;
        if (cameraX < 0) cameraX = 0;
    }

    private void resetPlayer() {
        playerX = 50; playerY = 400; velY = 0;
        jumping = false; cameraX = 0; left = false; right = false; jumpPressed = false;
    }

    @Override
    public void run() {
        while (true) {
            updatePhysics();
            repaint();
            try { Thread.sleep(16); } catch (Exception e) {}
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (gameState == MENU) drawMenu(g);
        else if (gameState == SETTINGS) drawSettings(g);
        else if (gameState == PLAYING) drawGame(g);
        else if (gameState == GAMEOVER) drawGameOver(g);
    }

    private void drawMenu(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 50));
        g.drawString("SUPER COLONZO WORLD", 130, 200);
        g.setFont(new Font("Monospaced", Font.PLAIN, 20));
        g.drawString("Press ENTER to Start", 290, 300);
    }

    private void drawGameOver(Graphics g) {
        g.setColor(Color.RED);
        g.setFont(new Font("Monospaced", Font.BOLD, 60));
        g.drawString("GAME OVER", 240, 250);
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.PLAIN, 20));
        g.drawString("Press ENTER for Menu", 290, 350);
    }

    private void drawSettings(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Monospaced", Font.BOLD, 30));
        g.drawString("CONTROLS", 320, 150);
        g.drawString("- A/D to Run", 300, 250);
        g.drawString("- SPACE to Jump", 300, 290);
    }

    private void drawGame(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;

        // Clouds with Shading
        for(int i = 0; i < 10; i++) {
            int cx = (i * 450) - (cameraX / 6);
            g2.setColor(Color.WHITE);
            g2.fillOval(cx, 100, 90, 45);
            g2.fillOval(cx + 25, 80, 60, 50);
            g2.setColor(new Color(240, 240, 240)); // Cloud Shadow
            g2.fillOval(cx + 10, 130, 70, 10);
        }

        g2.translate(-cameraX, 0);

        for (Platform p : platforms) {
            if (p.color == MARIO_GRASS) {
                for (int tx = p.x; tx < p.x + p.w; tx += 20) {
                    for (int ty = p.y; ty < p.y + p.h; ty += 20) {
                        g2.setColor(ty == p.y ? MARIO_GRASS : DIRT_BROWN);
                        g2.fillRect(tx, ty, 19, 19);
                        // Block Detail
                        g2.setColor(new Color(0,0,0,30));
                        g2.drawRect(tx, ty, 19, 19);
                    }
                }
            } else if (p.color == MARIO_BRICK) {
                for (int bx = p.x; bx < p.x + p.w; bx += 20) {
                    g2.setColor(MARIO_BRICK);
                    g2.fillRect(bx, p.y, 18, p.h - 1);
                    g2.setColor(new Color(80, 20, 5));
                    g2.drawRect(bx, p.y, 18, p.h - 1); // Dark brick outline
                }
            } else if (p.color == Q_BLOCK) {
                g2.setColor(Q_BLOCK);
                g2.fillRect(p.x, p.y, p.w, p.h);
                g2.setColor(Color.WHITE);
                g2.drawRect(p.x, p.y, p.w, p.h);
                g2.drawString("?", p.x + 14, p.y + 26);
            } else if (p.color == MARIO_PIPE) {
                g2.setColor(new Color(0, 100, 0));
                g2.fillRect(p.x, p.y, p.w, p.h);
                g2.setColor(MARIO_PIPE);
                g2.fillRect(p.x + 5, p.y, p.w - 10, p.h);
                g2.setColor(new Color(0, 80, 0));
                g2.fillRect(p.x - 5, p.y, p.w + 10, 25);
            }
        }

        // Mario Sprite
        int x = playerX, y = (int)playerY;
        g2.setColor(Color.BLUE); g2.fillRect(x + 4, y + 12, 24, 15);
        g2.setColor(Color.RED); g2.fillRect(x + 2, y + 14, 28, 8);
        g2.setColor(new Color(255, 200, 150)); g2.fillRect(x + 6, y + 2, 20, 12);
        g2.setColor(Color.RED); g2.fillRect(x + 6, y, 20, 5);
        if (facingRight) g2.fillRect(x + 18, y, 12, 5); else g2.fillRect(x + 2, y, 12, 5);
        g2.setColor(Color.BLACK); if (facingRight) g2.fillRect(x + 20, y + 5, 4, 4); else g2.fillRect(x + 8, y + 5, 4, 4);

        g2.translate(cameraX, 0);
        
        // HUD DESIGN
        g2.setColor(Color.WHITE);
        g2.setFont(new Font("Monospaced", Font.BOLD, 22));
        g2.drawString("FAYE", 30, 40);
        g2.drawString("WORLD 1-" + currentLevel, 30, 70);
        
        // LIVES DISPLAY
        g2.setColor(Color.RED);
        g2.drawString("♥ x " + lives, 30, 100);
    }
}