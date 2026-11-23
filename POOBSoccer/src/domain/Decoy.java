package domain;

import java.io.Serializable;

/** Balón falso de la Ilusión: solo se dibuja, no choca ni hace daño. */
public class Decoy implements Serializable {
    private double x, y, vx, vy;
    private final double radius;

    public Decoy(double x, double y, double vx, double vy, double radius) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.radius = radius;
    }

    /** Se mueve en línea recta y rebota en los bordes de la cancha. */
    public void update(double dt, Field field) {
        x += vx * dt;
        y += vy * dt;
        if (y < radius)                      { y = radius;                          vy = -vy; }
        if (y > field.getFloorY() - radius)  { y = field.getFloorY() - radius;      vy = -vy; }
        if (x < radius)                      { x = radius;                          vx = -vx; }
        if (x > field.getWidth() - radius)   { x = field.getWidth() - radius;       vx = -vx; }
    }

    public double getX() { return x; }
    public double getY() { return y; }
}
