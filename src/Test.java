import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Random;

public class Test {
    public static void main(String[] args) {
        MyGame g = new MyGame();
    }
}

class MyGame extends Game {
    private JFrame window;
    private JPanel panel;
    private Player player;
    private Projectile rocket;
    private BonusItem ammo;
    private int rockets;
    private Random random;

    public MyGame() {
        super();
        random = new Random();
        openWindow();
        player = new Player("plane.png");
        player.addSubpawn(new Projectile("shaverma.png"), -15, 0, -90);
        player.addSubpawn(new Projectile("shaverma.png"), 15, 0, -90);
        addPawn(player);
        ammo = new BonusItem("shaverma.png");
        ammo.visible = false;
        rocket = new Projectile("shaverma.png");
        new Thread(this).start();
        while (true) {
            if (random.nextDouble() < 0.01 && !ammo.visible) {
                spawnPawnAtLocation(ammo, random.nextDouble() * panel.getWidth(), random.nextDouble() * panel.getHeight(), -90);
                System.out.println("spawned ammo");
            }
            panel.repaint();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void openWindow() {
        window = new JFrame("Game");
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setSize(800, 600);
        panel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);
                Graphics2D g2d = (Graphics2D) g;
                setNewBounds(getWidth(), getHeight());
                renderHints(g2d);
                g2d.clearRect(0, 0, getWidth(), getHeight());
                render(g2d);
            }
        };
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (rockets > 0) {
                    rockets--;
                    spawnPawnAtLocation(rocket,player.getSubpawnGlobalX(rockets),player.getSubpawnGlobalY(rockets),0);
                    player.hideSubpawn(rockets);
                }
                super.mousePressed(e);
            }
        });
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                player.setMouse(e.getX(), e.getY());
                super.mouseMoved(e);
            }
        });
        window.add(panel);
        window.setVisible(true);
    }

    private void render(Graphics2D g2d) {
        renderGame(g2d);
        if (rocket != null) rocket.setAngle(player.getAngle());
    }

    private void renderHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    @Override
    public void pawnOutOfBounds(Pawn p, double nx, double ny) {
        if (p.getClass() == Projectile.class) {
            killPawn(p);
        }
        if (p.getClass() == Player.class) {
            p.addSpeed(nx, ny);
        }
    }

    @Override
    public void PawnCollide(Pawn p1, Pawn p2, double conv) {
        if (p1.getClass() == BonusItem.class || p2.getClass() == BonusItem.class) {
            if (p1.getClass() == Player.class || p2.getClass() == Player.class) {
                if (rockets < 2 && ammo.visible) {
                    player.appearSubpawn(rockets);
                    killPawn(ammo);
                    rockets++;
                }
            }
        } else if (conv < 0) {
            System.out.println(String.valueOf(conv));
            System.out.println(p1.getClass());
            System.out.println(p2.getClass());
            killPawn(p1);
            killPawn(p2);
        }
    }
}

class Player extends Pawn {
    private double mouseX, mouseY;

    Player(String imageFile) {
        super(imageFile);
        spawn(400, 300, 0);
    }

    public void setMouse(double mouseX, double mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
    }

    @Override
    public void move(double dt) {
        moveToTarget(mouseX, mouseY, 300, 300, dt, 1);
    }
}

class Projectile extends Pawn {
    private double angle;

    Projectile(String imageFile) {
        super(imageFile);
    }

    public void setAngle(double angle) {
        this.angle = angle;
    }

    @Override
    public void move(double dt) {
        moveDirection(500, angle, dt);
    }
}

class BonusItem extends Pawn {
    BonusItem(String imageFile) {
        super(imageFile);
    }
}