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
    public void start() {
        gameThread = new Thread(this);
        gameThread.start();
    }
    @Override
    public void run() {
        while (true) {
            repaint();
            try { Thread.sleep(16); } catch (Exception e) {}
        }
    }
}

private int playerX = 100, playerY = 100;

@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    g.setColor(Color.CYAN);
    g.fillRect(playerX, playerY, 32, 32);
}