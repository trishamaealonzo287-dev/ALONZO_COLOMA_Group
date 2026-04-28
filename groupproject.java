import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;

public class groupproject {
    public static void main(String[] args) {
        JFrame frame = new JFrame("Colonzo Pixel Obby");
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
    boolean isLava; // Property to determine if platform kills player

    public Platform(int x, int y, int w, int h, Color color, boolean isLava) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.color = color;
        this.isLava = isLava;
    }
}

class ObbyGame extends JPanel implements Runnable {
    private Thread gameThread;
    
    // --- GAME STATES ---
    private final int MENU = 0;
    private final int PLAYING = 1;
    private final int SETTINGS = 2;
    private int gameState = MENU;

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

    // Death counter variable
    private int deaths = 0;

    // CAMERA VARIABLE ADDED
    private int cameraX = 0;

    // Add to ObbyGame: 
    java.util.List<Platform> platforms = new java.util.ArrayList<>();

    public ObbyGame() {
        this.setPreferredSize(new Dimension(800, 600));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);

        // --- Define Level Platforms ---
        loadLevel(currentLevel);
        
        this.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                // Menu Navigation Controls
                if (gameState == MENU) {
                    if(e.getKeyCode() == KeyEvent.VK_ENTER) {
                        currentLevel = 1;
                        loadLevel(currentLevel);
                        gameState = PLAYING;
                    }
                    if(e.getKeyCode() == KeyEvent.VK_S) gameState = SETTINGS;
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
        resetPlayer(); // Ensure player position and inputs are reset for new level
        
        switch(level) {
            case 1:
                platforms.add(new Platform(0, 500, 400, 50, Color.DARK_GRAY, false));
                platforms.add(new Platform(450, 400, 200, 20, Color.DARK_GRAY, false));
                platforms.add(new Platform(700, 300, 150, 20, Color.YELLOW, false));
                break;
            case 2:
                platforms.add(new Platform(0, 500, 200, 50, Color.DARK_GRAY, false));
                platforms.add(new Platform(200, 550, 400, 50, Color.RED, true)); 
                platforms.add(new Platform(300, 350, 100, 20, Color.DARK_GRAY, false));
                platforms.add(new Platform(650, 400, 150, 20, Color.YELLOW, false));
                break;
            case 3:
                platforms.add(new Platform(0, 500, 100, 50, Color.DARK_GRAY, false));
                platforms.add(new Platform(200, 450, 50, 20, Color.DARK_GRAY, false));
                platforms.add(new Platform(400, 400, 50, 20, Color.DARK_GRAY, false));
                platforms.add(new Platform(600, 350, 50, 20, Color.DARK_GRAY, false));
                platforms.add(new Platform(800, 300, 100, 20, Color.YELLOW, false));
                break;
            case 4:
                // Section 3 & 4 inspired layout
                platforms.add(new Platform(0, 580, 1200, 20, Color.RED, true)); 
                platforms.add(new Platform(50, 500, 100, 20, Color.DARK_GRAY, false));
                platforms.add(new Platform(250, 400, 100, 20, Color.DARK_GRAY, false));
                platforms.add(new Platform(450, 300, 100, 20, Color.DARK_GRAY, false));
                platforms.add(new Platform(700, 250, 100, 20, Color.YELLOW, false));
                break;
            case 5:
                platforms.add(new Platform(0, 500, 100, 50, Color.DARK_GRAY, false));
                platforms.add(new Platform(150, 580, 1500, 20, Color.RED, true));
                platforms.add(new Platform(200, 420, 60, 20, Color.DARK_GRAY, false));
                platforms.add(new Platform(400, 340, 60, 20, Color.DARK_GRAY, false));
                platforms.add(new Platform(600, 420, 60, 20, Color.DARK_GRAY, false));
                platforms.add(new Platform(850, 300, 200, 20, Color.YELLOW, false));
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
        // Only run physics if the game is active
        if (gameState != PLAYING) return;

        // MOVEMENT LOGIC: This uses the boolean variables to move the player left or right
        if (left) playerX -= 6;
        if (right) playerX += 6;

        // Gravity logic
        velY += GRAVITY;
        playerY += velY;

        // JUMP LOGIC: Uses your provided code
        if (jumpPressed && !jumping) {
            velY = -12;
            jumping = true;
        }

        boolean touchingLava = false;

        // COLLISION LOGIC: Using Rectangles
        Rectangle playerBounds = new Rectangle(playerX, (int)playerY, 32, 32);
        for (Platform p : platforms) {
            Rectangle platBounds = new Rectangle(p.x, p.y, p.w, p.h);
            
            if (playerBounds.intersects(platBounds)) {
                // Check if the platform is lava
                if(p.isLava) {
                    touchingLava = true;
                }

                // Only trigger landing collision if falling DOWN into a non-lava platform
                if (velY > 0 && !p.isLava) {
                    velY = 0;
                    playerY = p.y - 32; // Snap to top
                    jumping = false;

                    // Win Condition check
                    if (p.color == Color.YELLOW) {
                        if (currentLevel < MAX_LEVEL) {
                            currentLevel++;
                            loadLevel(currentLevel);
                        } else {
                            JOptionPane.showMessageDialog(this, "You Beat the Obby!");
                            resetPlayer();
                            gameState = MENU; // Return to menu after win
                        }
                        return;
                    }
                }
            }
        }

        // --- DEATH & RESPAWN LOGIC ---
        if (playerY > 600 || touchingLava) {
            resetPlayer();
            deaths++;
        }

        // CAMERA CALCULATION
        cameraX = playerX - 300;
        if (cameraX < 0) cameraX = 0;
    }

    private void resetPlayer() {
        playerX = 50;
        playerY = 400;
        velY = 0;
        jumping = false;
        cameraX = 0;
        // Added to prevent automatic movement on reset
        left = false;
        right = false;
        jumpPressed = false;
    }

    @Override
    public void run() {
        while (true) {
            updatePhysics();
            repaint();
            try { 
                Thread.sleep(16); // Approx 60 FPS
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        if (gameState == MENU) {
            drawMenu(g);
        } else if (gameState == SETTINGS) {
            drawSettings(g);
        } else if (gameState == PLAYING) {
            drawGame(g);
        }
    }

    private void drawMenu(Graphics g) {
        g.setColor(Color.CYAN);
        g.setFont(new Font("Arial", Font.BOLD, 50));
        g.drawString("PIXEL OBBY", 250, 200);
        
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.setColor(Color.WHITE);
        g.drawString("Press ENTER to Play", 300, 300);
        g.drawString("Press S for Settings", 305, 340);
    }

    private void drawSettings(Graphics g) {
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 30));
        g.drawString("SETTINGS", 320, 150);
        
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString("- Use A & D to move", 300, 250);
        g.drawString("- Use SPACE to jump", 300, 280);
        g.drawString("- Avoid Red (Lava)", 300, 310);
        g.drawString("- Reach Yellow (Goal)", 300, 340);
        
        g.drawString("Press ESC to return", 315, 450);
    }

    private void drawGame(Graphics g) {
        // In paintComponent:
        // Translate the view based on cameraX to create a scrolling effect
        g.translate(-cameraX, 0);

        // Draw Platforms
        for (Platform p : platforms) {
            g.setColor(p.color);
            g.fillRect(p.x, p.y, p.w, p.h);
        }

        // Draw Player
        g.setColor(Color.CYAN);
        g.fillRect(playerX, (int)playerY, 32, 32);

        // Reset translation so the HUD (UI) stays fixed to the screen
        g.translate(cameraX, 0);
        
        // DRAW DEATH COUNTER & LEVEL
        g.setColor(Color.WHITE);
        g.drawString("Level: " + currentLevel, 20, 20);
        g.drawString("Deaths: " + deaths, 20, 40);
        g.drawString("ESC for Menu", 700, 20);
    }
}