package domain;

public class Ball extends Physics{
    private static final double RADIUS = 20; // Tamaño del balon
    private static final double RESTITUTION = 0.78;
    private static final double GROUND_FRICTION = 0.95;
    private Shot shot;


    public Ball(double startX, double startY) {
        drag = 0.999;
        placeAt(startX, startY);
    }

    public void update(double dt, Field field) {
        if (shot != null){ shot.tick(dt, field);}
        boolean ownMotion = shot != null && shot.isFlying() && shot.move(this, dt, field);
        if (!ownMotion){ integrate(dt);}
        collideWithField(field);
        if (shot != null && shot.isFlying() && touchesSideWall(field)) {shot.endFlight();}
        if (shot != null && shot.isFinished()){shot = null;}
    }

    private boolean touchesSideWall(Field field) {
        return x - RADIUS <= 0 || x + RADIUS >= field.getWidth();
    }

    //Collide of the ball in the match
    public void collideWithField(Field field) {
        Collisions.bounceOffBounds(this, field, RESTITUTION, GROUND_FRICTION);
        Collisions.bounceOffCrossbars(this, field, RESTITUTION);
    }

    /** Rebote contra un personaje (círculo que se mueve con velocidad cvx, cvy). */
        public boolean collideWithCircle(double cx, double cy, double cr, double cvx, double cvy) {
        return Collisions.bounceOffCircle(this, RESTITUTION, cx, cy, cr, cvx, cvy);
    }


    public void reset(double newX, double newY) {
        placeAt(newX, newY);
        shot = null;
    }

    /** Lanza el balón con un tiro especial. */
    public void launch(Shot newShot) {
        shot = newShot;
        shot.start(this);
    }

    /** Un personaje tocó el balón: si va en tiro especial, el tiro le pega. */
    public void hitBy(Character who) {
        if (shot != null && shot.isFlying()) {shot.hit(who);}
    }

    public Shot getShot() {
        return shot;
    }

    public double getRadius(){
        return RADIUS;
    }
}