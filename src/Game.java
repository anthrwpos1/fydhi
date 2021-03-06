import java.awt.*;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

public abstract class Game implements Runnable {
    private ArrayList<Pawn> pawnList;
    private double limitX, limitY;
    private long T0, T;
    public boolean cont = true;

    public Game() {
        init();
    }

    public void init() {
        pawnList = new ArrayList<>();
    }

    public void setNewBounds(double limitX, double limitY) {
        this.limitX = limitX;
        this.limitY = limitY;
    }

    public void PawnCollide(Pawn p1, Pawn p2, double conv) {
    }

    public void pawnOutOfBounds(Pawn p, double nx, double ny) {
    }

    public void killPawn(Pawn p) {
        pawnList.remove(p);
        p.visible = false;
    }

    public void spawnPawnAtLocation(Pawn p, double x, double y, double defaultAngle) {
        pawnList.add(p);
        p.spawn(x, y, defaultAngle);
        p.visible = true;
    }

    public void addPawn(Pawn p) {
        pawnList.add(p);
    }

    public void run() {
        T0 = System.nanoTime();
        while (cont) {
            T = System.nanoTime();
            double dt = (double) (T - T0) * 1e-9;
            if (dt < 0) dt = 1e-9;
            T0 = T;
            try {
                Thread.sleep(1);
            } catch (InterruptedException e) {
            }
            for (int i = 0; i < pawnList.size(); i++) {
                Pawn p = pawnList.get(i);
                p.control(dt);
                double size = p.getSize();
                if (p.getX() < size / 2) {
                    pawnOutOfBounds(p, 1, 0);
                } else if (p.getX() > limitX - size / 2) {
                    pawnOutOfBounds(p, -1, 0);
                } else if (p.getY() < size / 2) {
                    pawnOutOfBounds(p, 0, 1);
                } else if (p.getY() > limitY - size / 2) {
                    pawnOutOfBounds(p, 0, -1);
                }
                for (int j = i + 1; j < pawnList.size(); j++) {
                    Pawn q = pawnList.get(j);
                    try {//todo в этом цикле куда-то пропадают павны
                        double dx = p.getX() - q.getX();
                        double dy = p.getY() - q.getY();
                        double dux = p.getUX() - q.getUX();
                        double duy = p.getUY() - q.getUY();
                        double dr = Math.sqrt(dx * dx + dy * dy);
                        double size2 = p.getSize() / 2 + q.getSize() / 2;
                        if (dr < size2) {
                            if (p.source != q && q.source != p && p.collisionEnable && q.collisionEnable) {
                                PawnCollide(p, q, dux * dx + duy * dy);
                            }
                        } else if (p.source == q) p.source = null;
                        else if (q.source == p) q.source = null;
                    } catch (java.lang.NullPointerException e) {
                        System.out.printf("pawns %d, %d\n", i, j);
                        System.out.println(String.valueOf(p));
                        System.out.println(String.valueOf(q));
                        System.exit(1);
                    }
                }
            }
        }
    }

    public void renderGame(Graphics2D g2d) {
        for (int i = 0; i < pawnList.size(); i++) {
            pawnList.get(i).render(new AffineTransform(), g2d);
        }
    }
}
