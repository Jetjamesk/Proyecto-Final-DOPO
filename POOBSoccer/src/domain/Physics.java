package domain;

public abstract class Physics implements java.io.Serializable{
    public static final double GRAVITY = 2500.0;

    protected double x, y;
    protected double vx, vy;
    protected double drag = 1.0;   // Ball usa 0.999; Character no tiene resistencia


    protected void integrate(double dt) {
        vy += GRAVITY * dt;
        vx *= drag;
        vy *= drag;
        x  += vx * dt;
        y  += vy * dt;
    }


    protected void placeAt(double nx, double ny) {
        x = nx; y = ny;
        vx = 0; vy = 0;
    }

    public abstract double getRadius();

    public double getX()  { return x; }
    public double getY()  { return y; }
    public double getVx() { return vx; }
    public double getVy() { return vy; }
}