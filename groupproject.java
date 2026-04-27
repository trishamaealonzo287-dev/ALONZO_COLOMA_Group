import javax.swing.*;
import java.awt.*;

public class ObbyGame extends JPanel {
    public ObbyGame() {
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