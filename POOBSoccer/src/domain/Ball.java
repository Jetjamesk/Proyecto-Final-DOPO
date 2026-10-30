package domain;

public class Ball {
    public double x, y;
    public double vx, vy;
    public final double radius = 20; // Slightly larger so it's easy to see

    private static final double GRAVITY = 2500.0;
    private static final double RESTITUTION = 0.78;
    private static final double AIR_DRAG = 0.999;
    private static final double GROUND_FRICTION = 0.95;

    public Ball(double startX, double startY) {
        this.x = startX;
        this.y = startY;
    }

    public void update(double dt, int floorY, int ceilingY, int leftWall, int rightWall) {
        vy += GRAVITY * dt;
        vx *= AIR_DRAG;
        vy *= AIR_DRAG;
        x += vx * dt;
        y += vy * dt;
        collide(floorY, ceilingY, leftWall, rightWall);
    }

    private void collide(int floorY, int ceilingY, int leftWall, int rightWall) {
        if (y + radius > floorY) {
            y = floorY - radius;
            vy = -vy * RESTITUTION;
            vx *= GROUND_FRICTION;
            if (Math.abs(vy) < 20) vy = 0;
        }
        if (y - radius < ceilingY) {
            y = ceilingY + radius;
            vy = -vy * RESTITUTION;
        }
        if (x - radius < leftWall) {
            x = leftWall + radius;
            vx = -vx * RESTITUTION;
        }
        if (x + radius > rightWall) {
            x = rightWall - radius;
            vx = -vx * RESTITUTION;
        }
    }
    public double getX(){
        return x;
    }
    public double getY(){
        return y;
    }
    public void setVX(double newVX){
        this.vx = newVX;
    }
    public void setVY(double newVY){
        this.vy = newVY;
    }
}

