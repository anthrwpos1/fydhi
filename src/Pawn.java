import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class Pawn {
    private double x, y, ux, uy;
    private BufferedImage image;
    private ArrayList<Pawn> subpawn;
    private double defaultAngle;
    public boolean visible = true;
    public double xlim, ylim;

    Pawn(String imageFile) {
        try {
            image = ImageIO.read(new File(imageFile));
        } catch (IOException e) {
            e.printStackTrace();
            System.exit(1);
        }
        subpawn = new ArrayList<>();
    }

    public void addSubpawn(Pawn p, double x, double y, double defaultAngle) {
        p.spawn(x, y, defaultAngle);
        subpawn.add(p);
    }

    public void removeSubspawn(int i) {
        subpawn.remove(i);
    }

    public Pawn releaseSubspawn(int i, double newDefaultAngle) {
        Pawn pawn = subpawn.get(i);
        subpawn.remove(i);
        double u = Math.sqrt(ux * ux + uy * uy);
        pawn.x = this.x + pawn.x * ux / u;
        pawn.y = this.y + pawn.y * uy / u;
        pawn.defaultAngle = newDefaultAngle;
        return pawn;
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

    public double getAngle() {
        return Math.atan2(uy, ux);
    }

    public void moveToTarget(double targetX, double targetY, double maxSpeed, double maxSpeedDistance, double dt, double inertia) {
        double dx = targetX - x;
        double dy = targetY - y;
        double distance = Math.sqrt(dx * dx + dy * dy);
        if (distance < maxSpeedDistance) {
            ux = ux + (dx / maxSpeedDistance * maxSpeed - ux) * (1 - inertia * dt);
            uy = uy + (dy / maxSpeedDistance * maxSpeed - uy) * (1 - inertia * dt);
        } else {
            ux = ux + (dx / distance * maxSpeed - ux) * (1 - inertia * dt);
            uy = uy + (dy / distance * maxSpeed - uy) * (1 - inertia * dt);
        }
        x = x + ux * dt;
        y = y + uy * dt;
    }

    public void moveWithSpeed(double ux, double uy, double dt) {
        x = x + ux * dt;
        y = y + uy * dt;
        if (x < 0 || x > xlim || y < 0 || y > ylim) visible = false;
    }

    public void moveDirection(double u, double angle, double dt) {
        ux = u * Math.cos(angle);
        uy = u * Math.sin(angle);
        x = x + ux * dt;
        y = y + uy * dt;
        if (x < 0 || x > xlim || y < 0 || y > ylim) {
            visible = false;
        }
    }

    public void render(AffineTransform at, Graphics2D g) {
        double angle = Math.atan2(uy, ux) + Math.PI / 2;
        AffineTransform preserved = new AffineTransform(at);
        preserved.translate(x, y);
        preserved.rotate(angle + defaultAngle);
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
