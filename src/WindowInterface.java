import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Random;

public class WindowInterface extends JFrame {
    public Panel p;

    WindowInterface(String wname) {
        super(wname);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    public void wOpen() {
        setSize(800, 600);
        p = new Panel();
        add(p);
        p.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                p.move = true;
                super.mouseEntered(e);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                p.move = false;
                super.mouseExited(e);
            }
        });
        p.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                p.mousex = e.getX();
                p.mousey = e.getY();
                super.mouseMoved(e);
            }
        });
        setVisible(true);
    }
}

class Panel extends JPanel {
    public int mousex, mousey;
    private Random random;
    private double bobbitX = 400, bobbitY = 300;
    private double littleAukX = 100, littleAukY = 200;
    private double littleAukUX = 0, littleAukUY = 0;
    private int littleAuksCount = 0;
    private double angle;
    public double dt;
    public boolean move;
    private BufferedImage bobbit;
    private BufferedImage littleAuk;
    private Graphics2D g2d;

    Panel() {
        random = new Random();
        File f = new File(".");
        System.out.println(f.getAbsolutePath());
        try {
            bobbit = ImageIO.read(new File("plane.jpg"));
            littleAuk = ImageIO.read(new File("rocket.jpg"));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    @Override
    public void paint(Graphics g) {
        g.clearRect(0, 0, getWidth(), getHeight());
        double dx = (double) mousex - bobbitX;
        double dy = (double) mousey - bobbitY;
        double mouseDistance = Math.sqrt(dx * dx + dy * dy);
        angle = Math.atan2(dy, dx) + Math.PI / 2;
        double speed = 0;
        if (mouseDistance > 100) speed = 100;
        else speed = mouseDistance;
        bobbitX += (dx / mouseDistance * speed * dt);
        bobbitY += (dy / mouseDistance * speed * dt);
        dx = littleAukX - bobbitX;
        dy = littleAukY - bobbitY;
        double littleAukDistance = Math.sqrt(dx * dx + dy * dy);
        littleAukUX += (random.nextDouble() - 0.5) * 5000 * dt;
        littleAukUY += (random.nextDouble() - 0.5) * 5000 * dt;
        double littleAukSpeed = Math.sqrt(littleAukUY * littleAukUY + littleAukUX * littleAukUX) + 1e-6;
//        if (littleAukSpeed > 200) {
            littleAukUX = littleAukUX / littleAukSpeed * 300;
            littleAukUY = littleAukUY / littleAukSpeed * 300;
//        }
        littleAukX = littleAukX + littleAukUX * dt;
        littleAukY = littleAukY + littleAukUY * dt;
        if (littleAukX < 0) {
            littleAukX = 0;
            littleAukUX = 0;
        }
        if (littleAukX > getWidth()) {
            littleAukX = getWidth();
            littleAukUX = 0;
        }
        if (littleAukY < 0) {
            littleAukY = 0;
            littleAukUY = 0;
        }
        if (littleAukY > getHeight()) {
            littleAukY = getHeight();
            littleAukUY = 0;
        }
        if (littleAukDistance < 30) {
            littleAukX = random.nextDouble() * getWidth();
            littleAukY = random.nextDouble() * getHeight();
            littleAukUX = 0;
            littleAukUY = 0;
            littleAuksCount++;
        }
        g2d = (Graphics2D) g;
        renderHints(g2d);
        renderImage(bobbitX, bobbitY, angle, bobbit);
        renderImage(littleAukX, littleAukY, Math.atan2(littleAukUY, littleAukUX) + Math.PI / 2, littleAuk);
        g2d.drawLine(mousex - 10, mousey, mousex + 10, mousey);
        g2d.drawLine(mousex, mousey - 10, mousex, mousey + 10);
        g2d.drawString(String.format("Умер раз: %d",littleAuksCount),10,30);
    }

    private void renderImage(double shiftX, double shiftY, double angle, BufferedImage bi) {
        AffineTransform at = new AffineTransform();
        at.translate(shiftX, shiftY);
        at.rotate(angle);
        at.translate(-bi.getWidth() / 2, -bi.getHeight() / 2);
        g2d.drawImage(bi, at, null);
    }

    private void renderHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }
}