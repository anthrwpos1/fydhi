import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
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

            @Override
            public void mousePressed(MouseEvent e) {
                p.launchRocket();
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
    private Pawn user;
    private Pawn rocket;

    private Random random;
    private int FragCount = 0;
    public double dt;
    public boolean move;
    private Graphics2D g2d;

    public void launchRocket() {
        rocket = user.releaseSubspawn(0,0);
        rocket.xlim = getWidth();
        rocket.ylim = getHeight();
        rocket.visible = true;
        System.out.printf("rocke x=%f, y=%f",rocket.getX(),rocket.getY());
    }

    Panel() {
        random = new Random();
        user = new Pawn("plane.jpg");
//        rocket = new Pawn("rocket.jpg");
//        rocket.visible = false;
        user.addSubpawn(new Pawn("rocket.jpg"), 40, 20, -90);
        user.addSubpawn(new Pawn("rocket.jpg"), 10, 20, -90);
        user.spawn(100, 100, 0);
    }

    @Override
    public void paint(Graphics g) {
        g.clearRect(0, 0, getWidth(), getHeight());
        g2d = (Graphics2D) g;
        renderHints(g2d);
        user.moveToTarget((double) mousex, (double) mousey, 30, 300, dt, 1);
        if (rocket != null) {
            if (rocket.visible) {
                rocket.moveDirection(50, user.getAngle(), dt);
            }
            rocket.render(new AffineTransform(), g2d);
            rocket.xlim = getWidth();
            rocket.ylim = getHeight();
        }
        user.render(new AffineTransform(), g2d);
        g2d.drawLine(mousex - 10, mousey, mousex + 10, mousey);
        g2d.drawLine(mousex, mousey - 10, mousex, mousey + 10);
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