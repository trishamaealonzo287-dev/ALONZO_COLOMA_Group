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
    // MOVED THE PLAYER VARIABLES HERE
    private int playerX = 100, playerY = 100;
    private boolean left = false, right = false; // Added these for movement

    public ObbyGame() {
        this.setFocusable(true);
        this.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent e) {
                if(e.getKeyCode() == java.awt.event.KeyEvent.VK_A) left = true;
                if(e.getKeyCode() == java.awt.event.KeyEvent.VK_D) right = true;
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

    @Override
    public void run() {
        while (true) {
            
            

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
    }
}