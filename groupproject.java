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
        frame.setLocationRelativeTo(null); // Center window on screen
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
    
    // Player Position and Physics
    private double playerX = 100, playerY = 100;
    private double velY = 0;
    private final double GRAVITY = 0.5;
    
    // States
    private boolean left = false, right = false, space = false;
    private boolean jumping = false;
    
    // Camera
    private int cameraX = 0;

    // Level Data
    private List<Platform> platforms = new ArrayList<>();

    public ObbyGame() {
        this.setPreferredSize(new Dimension(800, 600));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);

        // --- Create the Level ---
        platforms.add(new Platform(50, 500, 400, 30, Color.DARK_GRAY));   // Start
        platforms.add(new Platform(500, 420, 150, 20, Color.DARK_GRAY));  // Jump 1
        platforms.add(new Platform(750, 340, 150, 20, Color.DARK_GRAY));  // Jump 2
        platforms.add(new Platform(1000, 260, 200, 20, Color.DARK_GRAY)); // Jump 3
        platforms.add(new Platform(1300, 200, 100, 20, Color.YELLOW));    // Goal

        // --- Input Handling ---
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_A) left = true;
                if (e.getKeyCode() == KeyEvent.VK_D) right = true;
                if (e.getKeyCode() == KeyEvent.VK_SPACE) space = true;
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_A) left = false;
                if (e.getKeyCode() == KeyEvent.VK_D) right = false;
                if (e.getKeyCode() == KeyEvent.VK_SPACE) space = false;
            }
        });
    }

    public void start() {
        if (gameThread == null) {
            gameThread = new Thread(this);
            gameThread.start();
        }
    }

    private void update() {
        // Horizontal Movement
        if (left) playerX -= 6;
        if (right) playerX += 6;

        // Apply Gravity
        velY += GRAVITY;
        playerY += velY;

        // Collision Detection using Rectangles
        Rectangle playerBounds = new Rectangle((int) playerX, (int) playerY, 32, 32);
        
        for (Platform p : platforms) {
            Rectangle platBounds = new Rectangle(p.x, p.y, p.w, p.h);
            
            if (playerBounds.intersects(platBounds)) {
                // Only land if falling downward
                if (velY > 0) {
                    velY = 0;
                    playerY = p.y - 32; // Snap to top
                    jumping = false;

                    // Win Condition check
                    if (p.color == Color.YELLOW) {
                        JOptionPane.showMessageDialog(this, "You Beat the Obby!");
                        resetPlayer();
                        return; // Exit update to avoid further logic this frame
                    }
                }
            }
        }

        // Jump Logic
        if (space && !jumping) {
            velY = -12.0;
            jumping = true;
        }

        // Camera follows player
        cameraX = (int) playerX - 400;

        // Fall off screen reset
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
            update();
            repaint();
            try {
                Thread.sleep(16); // ~60 FPS
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Everything inside here is shifted by the camera
        g.translate(-cameraX, 0);

        // Draw Platforms
        for (Platform p : platforms) {
            g.setColor(p.color);
            g.fillRect(p.x, p.y, p.w, p.h);
        }

        // Draw Player
        g.setColor(Color.CYAN);
        g.fillRect((int) playerX, (int) playerY, 32, 32);
        
        // Optional: Simple background stars for depth
        g.setColor(Color.WHITE);
        for(int i = 0; i < 2000; i += 250) {
            g.fillOval(i, 50, 3, 3);
            g.fillOval(i + 100, 150, 3, 3);
        }
    }
}