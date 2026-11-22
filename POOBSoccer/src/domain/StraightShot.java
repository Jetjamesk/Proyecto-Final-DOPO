package domain;

/** Tiro recto y horizontal a la altura de la cabeza del lanzador. */
public abstract class StraightShot extends Shot {

    protected StraightShot(Character owner) {
        super(owner);
    }

    protected abstract double speed();

    @Override
    protected void start(Ball ball) {
        int dir = owner.getFacing();
        ball.placeAt(owner.getX() + dir * (owner.getRadius() + ball.getRadius() + 2), owner.getY());
        ball.vx = dir * speed();
    }

    @Override
    protected boolean move(Ball ball, double dt, Field field) {
        ball.vy = 0;
        ball.x += ball.vx * dt;
        return true;
    }
}
