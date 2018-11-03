import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
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
    private Projectile rocket;
    private BonusItem ammo;
    private int frag;
    private int rockets;
    private Random random;
    private NPC npc;
    private double mouseX, mouseY;

    public MyGame() {//конструктор игры. Выполняется первым.
        super();
        random = new Random();
        openWindow();   //создание окна, см. ниже.
        player = new Player("destroyer.png");   //отсюда всё выполняется после создания окна
        player.addSubpawn(new Projectile("rocket.png"), -15, 0, 0); //добавляем ракеты
        player.addSubpawn(new Projectile("rocket.png"), 15, 0, 0);  //под крылья
        spawnPawnAtLocation(player, 400, 300, 90);
        ammo = new BonusItem("shaverma.png");                                           //загружаем шаверму
        ammo.visible = false;
        Thread physics = new Thread(this);//запускаем физику отдельным потоком
        physics.start(); //эта команда исполняет метод run() класса Game, поскольку он implements Runnable
        while (player.isAlive) {                                                                //цикл интерфейса игры
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
                rocket.setOrientation(player.getAngle());           //направляем ракету куда смотрит игрок
            }
            panel.repaint();                    //отрисовываем сцену. см. метод paint(Graphics g) в методе openWindow();
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        cont = false;                                               //завершение игры
        window.dispose();
        System.exit(0);
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
                    rocket = new Projectile("rocket.png");  //выстрел ракетой
                    rocket.source = player;
                    spawnPawnAtLocation(rocket, player.getSubpawnGlobalX(rockets), player.getSubpawnGlobalY(rockets), 90);
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
        renderGame(g2d);    //рисуем содержимое окна. см. класс Game
        g2d.drawString(String.format("Frags: %d",frag),30,30);
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
        if (p.getClass() == Projectile.class) {
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
            killPawn(p1);//уничтожаем столкнувшиеся павны
            killPawn(p2);
            if (p1.getClass() == Projectile.class || p2.getClass() == Projectile.class) frag++;//засчитываем фраг
            if (p1.getClass() == Player.class || p2.getClass() == Player.class) player.isAlive = false;//game over...
            spawnPawnAtLocation(new BonusItem("shaverma.png"), p1.getX(), p1.getY(), 0);//на месте столкновения возникает шаверма
        }
    }
}

class Player extends Pawn {
    private double mouseX, mouseY;
    private final double acceleration = 500;
    public boolean isAlive = true;

    Player(String imageFile) {
        super(imageFile);
        spawn(400, 300, 0);
    }

    public void setMouse(double mouseX, double mouseY) {
        this.mouseX = mouseX;
        this.mouseY = mouseY;
        orientation = Math.atan2(mouseY - getY(), mouseX - getX());
    }


    @Override
    public void control(double dt) {
        super.control(dt);
        accelerate(Math.cos(orientation) * acceleration, Math.sin(orientation) * acceleration);
        friction(0, 0, 1, 1);
    }
}

class Projectile extends Pawn {
    private final double acceleration = 3000;

    Projectile(String imageFile) {
        super(imageFile);
    }

    public void setOrientation(double angle) {
        this.orientation = angle;
    }

    @Override
    public void control(double dt) {
        super.control(dt);
        accelerate(Math.cos(orientation) * acceleration, Math.sin(orientation) * acceleration);
        friction(0, 0, 1, 3);
    }
}

class BonusItem extends Pawn {
    BonusItem(String imageFile) {
        super(imageFile);
    }
}

class NPC extends Pawn {
    private Random random;

    NPC(String imageFile, Random random) {
        super(imageFile);
        this.random = random;
    }

    @Override
    public void control(double dt) {
        super.control(dt);
        accelerate((random.nextDouble() - 0.5) * 10000, (random.nextDouble() - 0.5) * 10000);
        orientation = Math.atan2(getUY(), getUX());
        friction(0, 0, 1, 1);
    }
}