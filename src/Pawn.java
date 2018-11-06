import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public abstract class Pawn implements Cloneable {
    private double x, y, ux, uy;
    public double boundX, boundY;
    private ArrayList<Pawn> subpawn;
    private double defaultAngle;
    public double orientation;
    public boolean visible = true;
    public Pawn source;
    public boolean collisionEnable;
    private double dt;

    public BufferedImage useImage(String imageFile) {
        BufferedImage image = null;
        try {
            image = ImageIO.read(new File(imageFile));
            boundX = image.getWidth();
            boundY = image.getHeight();
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        return image;
    }

    Pawn() {
        subpawn = new ArrayList<>();
    }

    public Pawn addSubpawn(Pawn p, double x, double y, double defaultAngle) {
        p.spawn(x + boundX / 2, y + (double) boundY / 2, defaultAngle);
        p.visible = false;
        subpawn.add(p);
        return this;
    }

    public void removeSubpawn(int i) {
        subpawn.remove(i);
    }

    public Pawn releaseSubpawn(int i, double newDefaultAngle) {
        if (i < subpawn.size()) {
            Pawn pawn = subpawn.get(i);
            pawn.visible = false;
            double u = Math.sqrt(ux * ux + uy * uy);
            double vecdx = pawn.getX() - boundX / 2;
            double vecdy = pawn.getY() - boundY / 2;
            pawn.spawn(this.x + ux / u * vecdy - uy / u * vecdx, this.y + uy / u * vecdy + ux / u * vecdx, newDefaultAngle);
            return pawn;
        }
        return null;
    }

    public double getSubpawnGlobalX(int i) {
        Pawn pawn = subpawn.get(i);
        double u = Math.sqrt(ux * ux + uy * uy);
        double vecdx = pawn.getX() - boundX / 2;
        double vecdy = -pawn.getY() + boundY / 2;
        return this.x + Math.cos(orientation) * vecdy - Math.sin(orientation) * vecdx;
    }

    public double getSubpawnGlobalY(int i) {
        Pawn pawn = subpawn.get(i);
        double u = Math.sqrt(ux * ux + uy * uy);
        double vecdx = pawn.getX() - boundX / 2;
        double vecdy = -pawn.getY() + boundY / 2;
        return this.y + Math.sin(orientation) * vecdy + Math.cos(orientation) * vecdx;
    }

    public void appearSubpawn(int i) {
        subpawn.get(i).visible = true;
    }

    public void hideSubpawn(int i) {
        subpawn.get(i).visible = false;
    }

    public void spawn(double x, double y, double defaultAngle) {
        this.x = x;
        this.y = y;
        this.defaultAngle = Math.PI / 180 * defaultAngle;
        ux = 0;
        uy = 0;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getUX() {
        return ux;
    }

    public double getUY() {
        return uy;
    }

    public double getSize() {
        return Math.sqrt(boundX * boundX + boundY * boundY) / 1.414213562;
    }

    public void control(double dt) {
        this.dt = dt;
        move();
    }

    public void setSpeedInDirection(double u, double angle) {
        ux = u * Math.cos(angle);
        uy = u * Math.sin(angle);
    }

    public void setSpeed(double ux, double uy) {
        this.ux = ux;
        this.uy = uy;
    }

    public void friction(double sourceUX, double sourceUY, int power, double frictionCoef) {
        double u = Math.sqrt(ux * ux + uy * uy);
        ux = ux - ux * Math.pow(u, power - 1) * dt * frictionCoef;
        uy = uy - uy * Math.pow(u, power - 1) * dt * frictionCoef;
    }

    public void accelerate(double ax, double ay) {
        ux = ux + ax * dt;
        uy = uy + ay * dt;
    }

    public void dragToLocation(double x, double y) {
        this.x = x;
        this.y = y;
    }

    private void move() {
        x = x + ux * dt;
        y = y + uy * dt;
    }

    public void render(AffineTransform at, Graphics2D g) {
        AffineTransform preserved = new AffineTransform(at);
        preserved.translate(x, y);
        preserved.rotate(orientation + defaultAngle);
        preserved.translate(-boundX / 2, -boundY / 2);
        if (visible) {
            for (int i = 0; i < subpawn.size(); i++) {
                Pawn sub = subpawn.get(i);
                sub.render(preserved, g);
            }
            drawPawn(preserved, g);
        }
    }

    public void drawPawn(AffineTransform at, Graphics2D g) {
    }
}
