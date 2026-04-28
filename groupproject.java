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

    public Platform(int x, int y, int w, int h, Color color) {
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;
        this.color = color;
    }
}

class ObbyGame extends JPanel implements Runnable {
    private Thread gameThread;
    
    // MOVED THE PLAYER VARIABLES HERE
    private double playerX = 100, playerY = 100;
    private double velY = 0;
    private final double GRAVITY = 0.5;
    
    // Added these for movement
    private boolean left = false, right = false;
    private boolean jumpPressed = false; 
    private boolean jumping = false;

    // Camera offset to follow player
    private int cameraX = 0;

    // Add to ObbyGame: 
    java.util.List<Platform> platforms = new java.util.ArrayList<>();

    public ObbyGame() {
        this.setPreferredSize(new Dimension(800, 600));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);

        // Define some platforms for the level
        platforms.add(new Platform(50, 500, 400, 30, Color.DARK_GRAY));   // Starting ground
        platforms.add(new Platform(500, 420, 150, 20, Color.DARK_GRAY));  // Middle platform
        platforms.add(new Platform(750, 340, 150, 20, Color.DARK_GRAY));  // High platform
        platforms.add(new Platform(1000, 260, 200, 20, Color.DARK_GRAY)); // Far platform
        platforms.add(new Platform(1300, 200, 100, 20, Color.YELLOW));    // GOAL platform

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

        // NEW JUMP LOGIC
        if (jumpPressed && !jumping) {
            velY = -12;
            jumping = true;
        }

        // COLLISION LOGIC: Using Rectangles
        Rectangle playerBounds = new Rectangle((int)playerX, (int)playerY, 32, 32);
        for (Platform p : platforms) {
            Rectangle platBounds = new Rectangle(p.x, p.y, p.w, p.h);
            if (playerBounds.intersects(platBounds)) {
                // Only trigger collision if falling DOWN into the platform
                if (velY > 0) {
                    velY = 0;
                    playerY = p.y - 32;
                    jumping = false;

                    // Win Condition check
                    if (p.color == Color.YELLOW) {
                        JOptionPane.showMessageDialog(this, "You Beat the Obby!");
                        resetPlayer();
                        return;
                    }
                }
            }
        }

        // Update Camera to follow player (centered)
        cameraX = (int)playerX - 400;

        // Reset if fall off screen
        if (playerY > 700) {
            resetPlayer();
        }
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
                Thread.sleep(16); // Approx 60 FPS
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    // MOVE PAINTCOMPONENT INSIDE HERE
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Translate the graphics context to simulate a camera
        g.translate(-cameraX, 0);

        // Draw Platforms
        for (Platform p : platforms) {
            g.setColor(p.color);
            g.fillRect(p.x, p.y, p.w, p.h);
        }

        // Draw Player
        g.setColor(Color.CYAN);
        g.fillRect((int)playerX, (int)playerY, 32, 32);
    }
}