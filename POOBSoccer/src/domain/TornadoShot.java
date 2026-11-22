package domain;

import java.sql.Time;

/** Nigeria - Tornado: remolino a ras de suelo que rebota rápido; deja inconsciente al rival. */
public class TornadoShot extends Shot {
    public static final double SPEED = 1100;
    public static final double DAMAGE = 8;
    public static final double KNOCKOUT_DURATION = 3.0;
    private static final double HOP_HEIGHT = 22;   // altura de cada rebote
    private static final double HOP_SPEED = 18;    // rebotes por segundo (rad/s)

    public TornadoShot(Character owner) { super(owner); }

    @Override public Type getType()     { return Type.TORNADO; }
    @Override public double getDamage() { return DAMAGE; }

    @Override
    protected void start(Ball ball) {
        int dir = owner.getFacing();
        ball.placeAt(owner.getX() + dir * (owner.getRadius() + ball.getRadius() + 2),
                owner.getFootY() - ball.getRadius());
        ball.vx = dir * SPEED;
    }

    @Override
    protected boolean move(Ball ball, double dt, Field field) {
        ball.vy = 0;
        ball.x += ball.vx * dt;
        ball.y = field.getFloorY() - ball.getRadius() - HOP_HEIGHT * Math.abs(Math.sin(HOP_SPEED * time));
        return true;
    }

    @Override
    protected void applyEffect(Character rival) {
        rival.applyUnconscious(KNOCKOUT_DURATION);
    }
}