import java.awt.*;
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
        frame.setVisible(true);
        game.start();
    }
}

class Platform {
    int x, y, w, h;
    public Platform(int x, int y, int w, int h) {
        this.x = x; this.y = y; this.w = w; this.h = h;
    }
}

class ObbyGame extends JPanel implements Runnable {
    private Thread gameThread;
    private double playerX = 100, playerY = 100;
    private double velY = 0;
    private final double GRAVITY = 0.5;
    private boolean left = false, right = false, space = false;

    // List to hold our platforms
    List<Platform> platforms = new ArrayList<>();

    public ObbyGame() {
        this.setPreferredSize(new Dimension(800, 600));
        this.setBackground(Color.BLACK);
        this.setFocusable(true);

        // Define some platforms for the level
        platforms.add(new Platform(50, 500, 200, 20));  // Starting ground
        platforms.add(new Platform(300, 400, 150, 20)); // Middle platform
        platforms.add(new Platform(500, 300, 150, 20)); // High platform

        this.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if(e.getKeyCode() == java.awt.event.KeyEvent.VK_A) left = true;
                if(e.getKeyCode() == java.awt.event.KeyEvent.VK_D) right = true;
                if(e.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE) space = true;
            }
            public void keyReleased(java.awt.event.KeyEvent e) {
                if(e.getKeyCode() == java.awt.event.KeyEvent.VK_A) left = false;
                if(e.getKeyCode() == java.awt.event.KeyEvent.VK_D) right = false;
                if(e.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE) space = false;
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
        velY += GRAVITY;
        playerY += velY;

        boolean onGround = false;

        // Collision Logic: Check every platform
        for (Platform p : platforms) {
            // Check if player is within horizontal bounds of platform
            if (playerX + 32 > p.x && playerX < p.x + p.w) {
                // Check if player is falling onto the top of the platform
                if (playerY + 32 >= p.y && playerY + 32 <= p.y + velY + GRAVITY) {
                    playerY = p.y - 32; // Snap to top
                    velY = 0;
                    onGround = true;
                }
            }
        }

        // Jump if on a platform and space is pressed
        if (onGround && space) {
            velY = -12.0;
        }

        // Reset if fall off screen
        if (playerY > 600) {
            playerX = 100;
            playerY = 100;
            velY = 0;
        }
    }

    @Override
    public void run() {
        while (true) {
            if (left) playerX -= 5;
            if (right) playerX += 5;
            updatePhysics();
            repaint();
            try { Thread.sleep(16); } catch (Exception e) {}
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // Draw Platforms
        g.setColor(Color.DARK_GRAY);
        for (Platform p : platforms) {
            g.fillRect(p.x, p.y, p.w, p.h);
        }

        // Draw Player
        g.setColor(Color.CYAN);
        g.fillRect((int)playerX, (int)playerY, 32, 32);
    }
}