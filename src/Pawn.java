import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public abstract class Pawn {
    private double x, y, ux, uy;
    private double boundX, boundY;
    private BufferedImage image;
    private ArrayList<Pawn> subpawn;
    private double defaultAngle;
    public double orientation;
    public boolean visible = true;
    public Pawn source;
    private double dt;

    Pawn(String imageFile) {
        try {
            image = ImageIO.read(new File(imageFile));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        subpawn = new ArrayList<>();
        boundX = image.getWidth();
        boundY = image.getHeight();
    }

    public Pawn addSubpawn(Pawn p, double x, double y, double defaultAngle) {
        p.spawn(x + image.getWidth() / 2, y + image.getHeight() / 2, defaultAngle);
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
            double vecdx = pawn.getX() - image.getWidth() / 2;
            double vecdy = pawn.getY() - image.getHeight() / 2;
            pawn.spawn(this.x + ux / u * vecdy - uy / u * vecdx, this.y + uy / u * vecdy + ux / u * vecdx, newDefaultAngle);
            return pawn;
        }
        return null;
    }

    public double getSubpawnGlobalX(int i) {
        Pawn pawn = subpawn.get(i);
        double u = Math.sqrt(ux * ux + uy * uy);
        double vecdx = pawn.getX() - image.getWidth() / 2;
        double vecdy = pawn.getY() - image.getHeight() / 2;
        return this.x + ux / u * vecdy - uy / u * vecdx;
    }

    public double getSubpawnGlobalY(int i) {
        Pawn pawn = subpawn.get(i);
        double u = Math.sqrt(ux * ux + uy * uy);
        double vecdx = pawn.getX() - image.getWidth() / 2;
        double vecdy = pawn.getY() - image.getHeight() / 2;
        return this.y + uy / u * vecdy + ux / u * vecdx;
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

    public double getAngle() {
        return Math.atan2(uy, ux);
    }

    public void control(double dt){
        this.dt = dt;
        move();
    }

    public void setDirection(double u, double angle) {
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
        preserved.translate(-image.getWidth() / 2, -image.getHeight() / 2);
        if (visible) {
            for (int i = 0; i < subpawn.size(); i++) {
                Pawn sub = subpawn.get(i);
                sub.render(preserved, g);
            }
            g.drawImage(image, preserved, null);
        }
    }
}
