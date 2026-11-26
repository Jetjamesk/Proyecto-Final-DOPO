package domain;

public final class Collisions {
    private Collisions() {}

    public static boolean bounceOffCircle(Physics b, double restitution,
                                          double cx, double cy, double cr,
                                          double cvx, double cvy) {
        double dx = b.x - cx,
               dy = b.y - cy;
        double dist = Math.hypot(dx, dy);
        double min = b.getRadius() + cr;
        if (dist >= min) {return false;}
        double nx = 0, 
               ny = -1;
        if (dist > 0) { nx = dx / dist; ny = dy / dist; }
        b.x = cx + nx * min;
        b.y = cy + ny * min;
        double rel = (b.vx - cvx) * nx + (b.vy - cvy) * ny;
        if (rel < 0) {
            b.vx -= (1 + restitution) * rel * nx;
            b.vy -= (1 + restitution) * rel * ny;
        }
        return true;
    }


    public static void bounceOffBounds(Physics b, Field f,
                                       double restitution, double groundFriction) {
        double r = b.getRadius();
        if (b.y + r > f.getFloorY()) {
            b.y = f.getFloorY() - r;
            b.vy = -b.vy * restitution;
            b.vx *= groundFriction;
            if (Math.abs(b.vy) < 20) {b.vy = 0;}
        }
        if (b.y - r < 0){ b.y = r;
            b.vy = -b.vy * restitution; }
        if (b.x - r < 0){ b.x = r;
            b.vx = -b.vx * restitution; }
        if (b.x + r > f.getWidth()) { b.x = f.getWidth() - r; b.vx = -b.vx * restitution; }
    }

    
    public static void bounceOffCrossbars(Physics b, Field f, double restitution) {
        double top = f.getGoalTopY();
        double leftX  = Math.max(0, Math.min(b.x, f.getGoalDepth()));
        double rightStart = f.getWidth() - f.getGoalDepth();
        double rightX = Math.max(rightStart, Math.min(b.x, f.getWidth()));
        bounceOffCircle(b, restitution, leftX,  top, 0, 0, 0);
        bounceOffCircle(b, restitution, rightX, top, 0, 0, 0);
    }
}