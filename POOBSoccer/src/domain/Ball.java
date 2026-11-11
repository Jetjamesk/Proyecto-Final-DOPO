package domain;

public class Ball {
    private static final double RADIUS = 20; // Tamaño del balon
    private double x, y;
    private double vx, vy;
    

    //Game physics
    private static final double GRAVITY = 2500.0;
    private static final double RESTITUTION = 0.78;
    private static final double AIR_DRAG = 0.999;
    private static final double GROUND_FRICTION = 0.95;

    public Ball(double startX, double startY) {
        reset(startX, startY);
    }

    public void update(double dt, Field field) {
        vy += GRAVITY * dt;
        vx *= AIR_DRAG;
        vy *= AIR_DRAG;
        x += vx * dt;
        y += vy * dt;
        collideWithField(field);
    }

    //Collide of the ball in the match
    public void collideWithField(Field field) {
        int floorY = field.getFloorY();
        if (y + RADIUS > floorY) {
            y = floorY - RADIUS;
            vy = -vy * RESTITUTION;
            vx *= GROUND_FRICTION;
            if (Math.abs(vy) < 20) vy = 0;
        }
        if (y - RADIUS < 0) {
            y = RADIUS;
            vy = -vy * RESTITUTION;
        }
        // Las paredes del fondo de cada arco
        if (x - RADIUS < 0) {
            x = RADIUS;
            vx = -vx * RESTITUTION;
        }
        if (x + RADIUS > field.getWidth()) {
            x = field.getWidth() - RADIUS;
            vx = -vx * RESTITUTION;
        }
        // Los travesaños son segmentos horizontales: el balón rebota en ellos
        pushOut(Math.max(0, Math.min(x, field.getGoalDepth())), field.getGoalTopY(), 0, 0, 0);
        double rightStart = field.getWidth() - field.getGoalDepth();
        pushOut(Math.max(rightStart, Math.min(x, field.getWidth())), field.getGoalTopY(), 0, 0, 0);
    }

    /** Rebote contra un personaje (círculo que se mueve con velocidad cvx, cvy). */
    public boolean collideWithCircle(double cx, double cy, double cr, double cvx, double cvy) {
        return pushOut(cx, cy, cr, cvx, cvy);
    }

    /** Si el balón se mete en el círculo (cx, cy, cr), lo saca y lo hace rebotar. */
    private boolean pushOut(double cx, double cy, double cr, double cvx, double cvy) {
        double dx = x - cx, dy = y - cy;
        double dist = Math.hypot(dx, dy);
        double min = RADIUS + cr;
        if (dist >= min) return false;
        double nx = 0, ny = -1;
        if (dist > 0) { nx = dx / dist; ny = dy / dist; }
        x = cx + nx * min;
        y = cy + ny * min;
        double relNormal = (vx - cvx) * nx + (vy - cvy) * ny;
        if (relNormal < 0) {
            vx -= (1 + RESTITUTION) * relNormal * nx;
            vy -= (1 + RESTITUTION) * relNormal * ny;
        }
        return true;
    }

    public void kick(double newVX,double newVY){
        this.vx = newVX;
        this.vy = newVY;
    }

    /** Deja el balón quieto en (newX, newY). */
    public void reset(double newX, double newY) {
        this.x = newX;
        this.y = newY;
        this.vx = 0;
        this.vy = 0;
    }

    public double getX(){
        return x;
    }
    public double getY(){
        return y;
    }
    public double getRadius(){
        return RADIUS;
    }
}