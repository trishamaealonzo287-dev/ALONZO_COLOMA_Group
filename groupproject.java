import java.awt.*;
import javax.swing.*;

public class groupproject extends JPanel {
    public groupproject() {
        this.setPreferredSize(new Dimension(800, 600));
        this.setBackground(Color.BLACK);
    }
    public static void main(String[] args) {
        JFrame frame = new JFrame("Colonzo Pixel Obby");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ObbyGame());
        frame.pack();
        frame.setVisible(true);
    }
}

class ObbyGame extends JPanel implements Runnable {
    private Thread gameThread;

    // Position and Physics Variables
    private double velY = 0;
    private final double GRAVITY = 0.5;

    // MOVED THE PLAYER VARIABLES HERE
    private int playerX = 100, playerY = 100;
    private boolean left = false, right = false; // Added these for movement

    public ObbyGame() {
        this.setFocusable(true);
        this.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if(e.getKeyCode() == java.awt.event.KeyEvent.VK_A) left = true;
                if(e.getKeyCode() == java.awt.event.KeyEvent.VK_D) right = true;

                // Jump Logic: Only jump if near the "ground"
                if(e.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE && playerY >= 500) {
                    velY = -12.0;
                }
            }
            public void keyReleased(java.awt.event.KeyEvent e) {
                if(e.getKeyCode() == java.awt.event.KeyEvent.VK_A) left = false;
                if(e.getKeyCode() == java.awt.event.KeyEvent.VK_D) right = false;
            }
        });
    }

    public void start() {
        gameThread = new Thread(this);
        gameThread.start();
    }

    private void updatePhysics() {
        // Gravity logic
        velY += GRAVITY;
        playerY += velY;

        // Simple Floor Collision (prevent falling off screen)
        if (playerY > 500) {
            playerY = 500;
            velY = 0;
        }
    }

    @Override
    public void run() {
        while (true) {
            // MOVEMENT LOGIC: This uses the boolean variables to move the player left or right
            if (left) playerX -= 5;
            if (right) playerX += 5;

            // Vertical Physics
            updatePhysics();
            
            repaint();
            try { 
                Thread.sleep(16); // Approx 60 FPS
            } catch (Exception e) {
                e.printStackTrace();
            }
        
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
        g.setColor(Color.CYAN);
        g.fillRect(playerX, playerY, 32, 32);

        // Draw a "floor" line
        g.setColor(Color.WHITE);
        g.drawLine(0, 532, 800, 532);
    }
}