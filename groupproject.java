import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
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
        // Adding your requested platforms (Defaulting to Gray and not lava)
        platforms.add(new Platform(0, 500, 200, 50, Color.DARK_GRAY, false));
        platforms.add(new Platform(300, 400, 150, 20, Color.DARK_GRAY, false));
        platforms.add(new Platform(550, 300, 150, 20, Color.DARK_GRAY, false));
        
        // Additional obstacles and Goal
        platforms.add(new Platform(800, 550, 300, 20, Color.RED, true)); // Lava pit
        platforms.add(new Platform(1100, 250, 150, 20, Color.DARK_GRAY, false));
        platforms.add(new Platform(1400, 200, 100, 20, Color.YELLOW, false)); // GOAL
        
        this.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_A) left = true;
                if(e.getKeyCode() == KeyEvent.VK_D) right = true;
                if(e.getKeyCode() == KeyEvent.VK_SPACE) jumpPressed = true;
            }
            public void keyReleased(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_A) left = false;
                if(e.getKeyCode() == KeyEvent.VK_D) right = false;
                if(e.getKeyCode() == KeyEvent.VK_SPACE) jumpPressed = false;
            }
        });
    }

    public void start() {
        if (gameThread == null) {
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    private void updatePhysics() {
        // MOVEMENT LOGIC: This uses the boolean variables to move the player left or right
        if (left) playerX -= 6;
        if (right) playerX += 6;

        velY += GRAVITY;
        playerY += velY;

        // JUMP LOGIC
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
                // Check for lava touch
                if(p.isLava) touchingLava = true;

                // Only land on top of solid platforms while falling
                if (velY > 0 && !p.isLava) {
                    velY = 0;
                    playerY = p.y - 32;
                    jumping = false;

                    // Win logic
                    if (p.color == Color.YELLOW) {
                        JOptionPane.showMessageDialog(this, "You Beat the Obby!");
                        resetPlayer();
                        return;
                    }
                }
            }
        }

        // DEATH & RESPAWN LOGIC
        if (playerY > 600 || touchingLava) {
            playerX = 100;
            playerY = 100;
            velY = 0;
            deaths++;
        }

        // CAMERA CALCULATION
        cameraX = playerX - 300;
    }

    private void resetPlayer() {
        playerX = 100;
        playerY = 100;
        velY = 0;
        jumping = false;
    }

    @Override
    public void run() {
        while (true) {
            updatePhysics();
            repaint();
            try { 
                Thread.sleep(16); 
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // MOVE PAINTCOMPONENT INSIDE HERE
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
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
        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 18));
        g.drawString("Deaths: " + deaths, 20, 30);
    }
}