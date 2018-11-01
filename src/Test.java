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
    private BonusItem frag;
    private int rockets;
    private Random random;
    private NPC npc;

    public MyGame() {
        super();
        random = new Random();
        openWindow();
        player = new Player("destroyer.png");
        player.addSubpawn(new Projectile("rocket.png"), -15, 0, -90);
        player.addSubpawn(new Projectile("rocket.png"), 15, 0, -90);
        addPawn(player);
        ammo = new BonusItem("shaverma.png");
        ammo.visible = false;
        new Thread(this).start();
        while (true) {
            if (random.nextDouble() < 0.01 && !ammo.visible) {
                spawnPawnAtLocation(ammo, random.nextDouble() * panel.getWidth(), random.nextDouble() * panel.getHeight(), -90);
            }

            if (random.nextDouble() < 0.001) {
                double newx = random.nextDouble() * panel.getWidth();
                double newy = random.nextDouble() * panel.getHeight();
                spawnPawnAtLocation(new NPC("plane.png", random), newx, newy, 0);
                System.out.printf("new npc %f, %f\n",newx,newy);
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
                    rocket = new Projectile("rocket.png");
                    rocket.source = player;
                    spawnPawnAtLocation(rocket, player.getSubpawnGlobalX(rockets), player.getSubpawnGlobalY(rockets), 0);
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
        if (p.getClass() == NPC.class) {
            p.addSpeed(nx*10, ny*10);
        }
    }

    @Override
    public void PawnCollide(Pawn p1, Pawn p2, double conv) {
        if (p1.getClass() == BonusItem.class || p2.getClass() == BonusItem.class) {
            if (p1.getClass() == Player.class || p2.getClass() == Player.class) {
                if (rockets < 2 && ammo.visible) {
                    player.appearSubpawn(rockets);
                    if (p1.getClass() == BonusItem.class) killPawn(p1);
                    if (p2.getClass() == BonusItem.class) killPawn(p2);
                    rockets++;
                }
            }
        } else {
            System.out.println(String.valueOf(conv));
            System.out.println(p1.getClass());
            System.out.println(p2.getClass());
            killPawn(p1);
            killPawn(p2);
            spawnPawnAtLocation(new BonusItem("shaverma.png"), p1.getX(), p1.getY(), 0);
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
        moveToTarget(mouseX, mouseY, 500, 300, dt, 0.3);
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
        moveDirection(1000, angle, dt);
    }
}

class BonusItem extends Pawn {
    BonusItem(String imageFile) {
        super(imageFile);
    }
}

class NPC extends Pawn {
    private Random random;
    private double angle;

    NPC(String imageFile, Random random) {
        super(imageFile);
        this.random = random;
    }

    @Override
    public void move(double dt) {
        moveWithSpeed(getUX(), getUY(), dt);
        addSpeed((random.nextDouble() - 0.5) * 30000 * dt, (random.nextDouble() - 0.5) *30000 * dt);
        double u = Math.sqrt(getUX() * getUX() + getUY() * getUY()) / 300;
        addSpeed(getUX() * (1 - u), getUY() * (1 - u));
    }
}