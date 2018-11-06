import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.util.Random;

public class Test {//стартовый класс

    public static void main(String[] args) {
        MyGame g = new MyGame();
    }//запускаем игру
}

class MyGame extends Game {
    private JFrame window;  //окно
    private JPanel panel;   //графическая область
    private Player player;
    private Missile rocket;
    private BonusItem ammo;
    private int frag;
    private int rockets;
    private Random random;
    private NPC npc;
    private double mouseX, mouseY;
    private double worldX = 0, worldY = -50;

    public MyGame() {//конструктор игры. Выполняется первым.
        super();
        random = new Random();
        openWindow();   //создание окна, см. ниже.
        play();
    }

    private void play() {
        frag = 0;
        rockets = 2;
        init();
        player = new Player("destroyer.png");   //отсюда всё выполняется после создания окна
        player.addSubpawn(new Missile("rocket.png"), -15, 0, 0); //добавляем ракеты
        player.addSubpawn(new Missile("rocket.png"), 15, 0, 0);  //под крылья
        player.addSubpawn(new TimedLife(1), 0, 20, 0);
        player.appearSubpawn(0);
        player.appearSubpawn(1);
        spawnPawnAtLocation(player, 400, 300, 90);
        ammo = new BonusItem("shaverma.png");                                           //загружаем шаверму
        ammo.visible = false;
        Thread physics = new Thread(this);//запускаем физику отдельным потоком
        physics.start(); //эта команда исполняет метод run() класса Game, поскольку он implements Runnable
        while (player.isAlive) {//цикл интерфейса игры
            if (Double.isNaN(player.getX())) {//todo костыль, пересоздающий игрока если его параметры NaN
                killPawn(player);
                spawnPawnAtLocation(player, 400, 400, 90);
                System.out.println("player NaN");
            }
            if (random.nextDouble() < 0.001 && !ammo.visible) {     //случайное появление шавермы
                spawnPawnAtLocation(ammo, random.nextDouble() * panel.getWidth(), random.nextDouble() * panel.getHeight(), -90);
            }

            if (random.nextDouble() < 0.001) {                      //случайное появление NPC
                double newx = random.nextDouble() * panel.getWidth();
                double newy = random.nextDouble() * panel.getHeight();
                spawnPawnAtLocation(new NPC("plane.png", random), newx, newy, 90);
            }
            player.setMouse(mouseX, mouseY);                        //передаем координаты курсора
            if (rocket != null) {
                rocket.setDirection(player.orientation);           //направляем ракету куда смотрит игрок
            }
            panel.repaint();                    //отрисовываем сцену. см. метод paint(Graphics g) в методе openWindow();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        cont = false;                                               //завершение игры
        int result = new JOptionPane().showOptionDialog(window, "player destroyed. Restart?", "result", JOptionPane.OK_OPTION, JOptionPane.INFORMATION_MESSAGE, null, new String[]{"Close", "Restart"}, "ok");
        if (result == 1) {
            cont = true;
            play();
        } else System.exit(0);
    }

    private void openWindow() {//Открытие окна
        window = new JFrame("Game");
        window.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        window.setSize(800, 600);
        panel = new JPanel() {
            @Override
            public void paint(Graphics g) {
                super.paint(g);     //этот метод исполняется при вызове panel.repaint();
                Graphics2D g2d = (Graphics2D) g;                    //получаем графику
                setNewBounds(getWidth(), getHeight());              //передаем границы игрового поля
                renderHints(g2d);                                   //режимы отрисовки
                g2d.clearRect(0, 0, getWidth(), getHeight());
                render(g2d);                                        //тут всё рисуем
            }
        };
        panel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (rockets > 0) {//этот метод исполняется при щелчке мышью в окне
                    rockets--;
                    rocket = new Missile("rocket.png");  //выстрел ракетой
                    rocket.source = player;
                    spawnPawnAtLocation(rocket, player.getSubpawnGlobalX(rockets), player.getSubpawnGlobalY(rockets), 90);
                    rocket.orientation = player.orientation;
                    rocket.setSpeed(player.getUX(), player.getUY());    //ракета появляется со скоростью игрока
                    player.hideSubpawn(rockets);                      //скрыть одну из ракет под крылом
                }
                super.mousePressed(e);
            }
        });
        panel.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();//этот метод исполняется при движении курсором внутри окна
                mouseY = e.getY();                                      //изменение координат курсора
                super.mouseMoved(e);
            }
        });
        window.add(panel);
        window.setVisible(true);
    }

    private void render(Graphics2D g2d) {
        g2d.setPaint(new GradientPaint(0, 0, new Color(0, 0, 128), 0, panel.getHeight(), new Color(64, 128, 255)));
        g2d.fillRect(0, 0, panel.getWidth(), panel.getHeight());
        renderGame(g2d);    //рисуем содержимое окна. см. класс Game
        g2d.setColor(Color.WHITE);
        g2d.drawString(String.format("Frags: %d", frag), 30, 30);
        g2d.drawString(String.format("PlayerCoord: %f/%f", player.getX(), player.getY()), 90, 30);
        TimedLife tl = new TimedLife(player.thrust / player.acceleration) {
            @Override
            public void timeOver() {
                killPawn(this);
            }
        };
        spawnPawnAtLocation(tl, player.getSubpawnGlobalX(2), player.getSubpawnGlobalY(2), 0);
        g2d.drawString(String.format("dt: %f", player.temp), 300, 30);
        tl.setSpeed(-player.thrust * Math.cos(player.orientation), -player.thrust * Math.sin(player.orientation));
    }

    private void renderHints(Graphics2D g) {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
    }

    @Override
    public void pawnOutOfBounds(Pawn p, double nx, double ny) {
        //этот метод исполняется при выходе любого павна за границы экрана.
        double bounceX = Math.abs(p.getUX()) * nx + p.getUX() * (1 - Math.abs(nx));//вычисляем отскок
        double bounceY = Math.abs(p.getUY()) * ny + p.getUY() * (1 - Math.abs(ny));
        if (p.getClass() == Missile.class) {
            killPawn(p); //ракета уничтожается
        }
        if (p.getClass() == Player.class) {
            p.setSpeed(bounceX, bounceY);//остальные - отскакивают
        }
        if (p.getClass() == NPC.class) {
            p.setSpeed(bounceX, bounceY);
        }
    }

    @Override
    public void PawnCollide(Pawn p1, Pawn p2, double conv) {
        //этот метод исполняется при сближении двух павнов ближе их радиусов.
        if (p1.getClass() == BonusItem.class || p2.getClass() == BonusItem.class) {//собираем шаверму
            if (p1.getClass() == Player.class || p2.getClass() == Player.class) {
                if (rockets < 2) {
                    player.appearSubpawn(rockets);
                    if (p1.getClass() == BonusItem.class) killPawn(p1);
                    if (p2.getClass() == BonusItem.class) killPawn(p2);
                    rockets++;
                }
            }
        } else {
            System.out.printf("collide %s, %s\n", String.valueOf(p1.getClass()), String.valueOf(p2.getClass()));
            killPawn(p1);//уничтожаем столкнувшиеся павны
            killPawn(p2);
            if (p1.getClass() == Missile.class || p2.getClass() == Missile.class) frag++;//засчитываем фраг
            if (p1.getClass() == Player.class || p2.getClass() == Player.class) player.isAlive = false;//game over...
            spawnPawnAtLocation(new BonusItem("shaverma.png"), p1.getX(), p1.getY(), 0);//на месте столкновения возникает шаверма
        }
    }
}

class Player extends Pawn {
    private double dX, dY;
    public final double acceleration = 500;
    private final double maxThrustDist2 = 40000;
    public boolean isAlive = true;
    private MAVG mavg;
    private Rotator vecToMouse = new Rotator(0);
    public double temp;
    private BufferedImage image;
    public double thrust;

    Player(String imageFile) {
        super();
        collisionEnable = true;
        image = useImage(imageFile);
        mavg = new MAVG(2, 0, 0.5, true, new Rotator(0));
    }

    public void setMouse(double mouseX, double mouseY) {
        dX = mouseX - getX();
        dY = mouseY - getY();
        vecToMouse = new Rotator(new Complex(dX, dY));
    }


    @Override
    public void control(double dt) {
        temp = dt;
        super.control(dt);
        mavg.dt = dt;
        mavg.mavg(vecToMouse);
        orientation = ((Rotator) mavg.dnout[0]).angle;
        double dr2 = (dX * dX + dY * dY);
        Rotator moveDir = new Rotator(Math.atan2(getUY(),getUX()));
        Rotator attackAngle = moveDir.sub(new Rotator(orientation));
        double speed = Math.sqrt(getUX()*getUX()+getUY()*getUY());
        double lift = Math.sin(-attackAngle.angle*2)*speed*3;
        double liftAngle = moveDir.add(new Rotator(Math.PI/2)).angle;
        double thrust = (dr2 > maxThrustDist2) ? acceleration : acceleration / maxThrustDist2 * dr2;
        accelerate(Math.cos(orientation) * thrust, Math.sin(orientation) * thrust);
        accelerate(0,300);
        accelerate(lift*Math.cos(liftAngle),lift*Math.sin(liftAngle));
        this.thrust = thrust;
        friction(0, 0, 1, 1);
    }

    @Override
    public void drawPawn(AffineTransform at, Graphics2D g) {
        g.drawImage(image, at, null);
    }
}

class Missile extends Pawn {
    private final double acceleration = 3000;
    private double fuel = 1;
    private double angle_intrtia = 10;
    private Rotator direction = new Rotator(0);
    private BufferedImage image;

    Missile(String imageFile) {
        super();
        collisionEnable = true;
        image = useImage(imageFile);
    }

    public void setDirection(double angle) {
        direction = new Rotator(angle);
    }

    @Override
    public void control(double dt) {
        Rotator curdir = new Rotator(orientation);
        super.control(dt);
        if ((fuel -= 0.1 * dt) > 0) {
            double angle_str = angle_intrtia * fuel * dt;
            orientation = direction.lerp(curdir, angle_str).angle;
            accelerate(Math.cos(orientation) * acceleration * Math.sqrt(fuel), Math.sin(orientation) * acceleration * Math.sqrt(fuel));
        }
        accelerate(0, 300);
        friction(0, 0, 1, 3);
    }

    @Override
    public void drawPawn(AffineTransform at, Graphics2D g) {
        g.drawImage(image, at, null);
    }
}

class BonusItem extends Pawn {
    private BufferedImage image;

    BonusItem(String imageFile) {
        super();
        collisionEnable = true;
        image = useImage(imageFile);
    }

    @Override
    public void drawPawn(AffineTransform at, Graphics2D g) {
        g.drawImage(image, at, null);
    }
}

class NPC extends Pawn {
    private Random random;
    private BufferedImage image;

    NPC(String imageFile, Random random) {
        super();
        collisionEnable = true;
        image = useImage(imageFile);
        this.random = random;
    }

    @Override
    public void control(double dt) {
        super.control(dt);
        double randx = random.nextDouble() - 0.5;
        double randy = random.nextDouble() - 0.5;
        double randmod = Math.sqrt(randx * randx + randy * randy) + 1e-6;
        accelerate(randx / randmod * 10000, randy / randmod * 10000);
        orientation = Math.atan2(getUY(), getUX());
        friction(0, 0, 1, 1);
    }

    @Override
    public void drawPawn(AffineTransform at, Graphics2D g) {
        g.drawImage(image, at, null);
    }
}

class TimedLife extends Pawn {
    public double lifeTime;
    private double timeFromExhaust;
    public double temp;

    TimedLife(double lifeTime) {
        super();
        this.lifeTime = lifeTime;
//        image = useImage("circle.png");
        boundX = 10;
        boundY = 10;
    }

    @Override
    public void control(double dt) {
        super.control(dt);
        friction(0, 0, 1, 1);
        if ((lifeTime -= dt) < 0) timeOver();
        timeFromExhaust += dt;
        temp = dt;
    }

    public void timeOver() {
    }

    @Override
    public void drawPawn(AffineTransform at, Graphics2D g) {
        g.setColor(colorOfPuffs());
        Point2D p;
        p = at.transform(new Point2D.Double(0, 0), null);
        g.fill(new Ellipse2D.Double(p.getX(), p.getY(), 10, 10));
//        g.drawImage(image,at,null);
    }

    private Color colorOfPuffs() {
        double red = 1;
        double green = 1 - Math.exp(-timeFromExhaust * 20);
        double blue = 1 - Math.exp(-timeFromExhaust * 10);
        double alpha = (lifeTime > 10) ? 1 : Math.sqrt(lifeTime/10);
        return new Color((int) (red * 255), (int) (green * 255), (int) (blue * 255), (int) (alpha * 255));
    }
}

class Rotator implements Fractional {//Арифметика поворотов
    //    public Complex unit;//комплексный вектор ориентации

    public double angle;
//    public double angle() {//угол ориентации
//        return unit.angle();

//    }

    public Rotator(Complex vector) {
        if (vector.abs() > 1e-16) {
            angle = vector.angle();
        } else angle = 0;
    }

    public Rotator(double angle) {
        this.angle = angle;
    }

    @Override
    public Rotator add(Fractional f) {//сложение поворотов
        Rotator s = (Rotator) f;
        return new Rotator(angle + s.angle);
    }

    @Override
    public Rotator sub(Fractional f) {//вычитание поворотов
        Rotator s = (Rotator) f;
        double diff = angle - s.angle;
        return new Rotator(diff - Math.round(diff / 2 / Math.PI) * 2 * Math.PI);
    }

    @Override
    public Rotator div(double d) {//деление поворотов
        return new Rotator(angle / d);
    }

    @Override
    public Rotator mul(double d) {//умножение поворотов
        return new Rotator(angle * d);
    }

    @Override
    public Rotator clone() {
        return new Rotator(angle);
    }

    public Rotator lerp(Rotator target, double x) {//интерполяция поворотов
        if (x < 0) x = 0;
        if (x > 1) x = 1;
        return sub(target).mul(x).add(target);
    }

}
