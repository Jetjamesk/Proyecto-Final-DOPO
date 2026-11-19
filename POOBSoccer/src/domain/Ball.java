package domain;

public class Ball extends Physics{
    private static final double RADIUS = 20; // Tamaño del balon
    private static final double RESTITUTION = 0.78;
    private static final double GROUND_FRICTION = 0.95;

    public Ball(double startX, double startY) {
        drag = 0.999;
        placeAt(startX, startY);
    }

    public void update(double dt, Field field) {
        integrate(dt);
        collideWithField(field);
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
    }


    public double getRadius(){
        return RADIUS;
    }
}